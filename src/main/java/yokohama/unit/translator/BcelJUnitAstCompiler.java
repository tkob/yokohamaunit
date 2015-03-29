package yokohama.unit.translator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.AnnotationEntryGen;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.TestMethod;

public class BcelJUnitAstCompiler implements JUnitAstCompiler {
    @Override
    public boolean compile(
            Path docyPath,
            CompilationUnit ast,
            String className,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            List<String> javacArgs) {
        ClassGen cg = new ClassGen(
                packageName.equals("") ? className : packageName + "." + className,
                "java.lang.Object", // super class
                docyPath.getFileName().toString(), // source file name
                Constants.ACC_PUBLIC | Constants.ACC_SUPER,
                null // implemented interfaces
        );
        // set class file version to Java 1.6
        cg.setMajor(50);
        cg.setMinor(0);
        
        ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
        cg.addEmptyConstructor(Constants.ACC_PUBLIC);

        for (TestMethod testMethod : ast.getClassDecl().getTestMethods()) {
            visitTestMethod(testMethod, cg, cp);
        }

        try {
            Path classFilePath = makeClassFilePath(dest, packageName, className);
            cg.getJavaClass().dump(classFilePath.toFile());
        } catch(java.io.IOException e) {
            System.err.println(e);
        }
        return true;
    }

    public Path makeClassFilePath(Optional<Path> dest, String packageName, String className) {
        Path classFile = (dest.isPresent() ? dest.get(): Paths.get("."))
                .resolve(Paths.get(packageName.replace('.', '/')))
                .resolve(className + ".class");
        return classFile;
    }

    private void visitTestMethod(TestMethod testMethod, ClassGen cg, ConstantPoolGen cp) {
        InstructionList il = new InstructionList();
        MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, // access flags
                Type.VOID, // return type of a test method is always void
                Type.NO_ARGS, new String[]{}, // test methods have no arguments
                testMethod.getName(),
                cg.getClassName(),
                il, cp);
        AnnotationEntryGen ag = new AnnotationEntryGen(
                new ObjectType("org.junit.Test"),
                Arrays.asList(),
                true,
                cp);
        mg.addAnnotationEntry(ag);
        InstructionFactory factory = new InstructionFactory(cg);

        for (Statement statement : testMethod.getStatements()) {
            visitStatement(statement, il, factory, cp);
        }

        il.append(InstructionConstants.RETURN);

        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    private void visitStatement(
            Statement statement,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        statement.<Void>accept(
            isStatement -> { return null; },
            isNotStatement -> { return null; },
            varInitStatement -> { return null; },
            returnIsStatement -> { return null; },
            returnIsNotStatement -> { return null; },
            invokeVoidStatement -> { return null; },
            tryStatement -> { return null; },
            ifStatement -> { return null; }
        );
    }
}