package yokohama.unit.translator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.AnnotationEntryGen;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.VarDeclVisitor;
import yokohama.unit.util.Pair;

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

        Map<String, LocalVariableGen> locals = new HashMap<>();
        for (Pair<yokohama.unit.ast_junit.Type,String> pair :
                VarDeclVisitor.sortedSet(new VarDeclVisitor().visitTestMethod(testMethod))) {
            yokohama.unit.ast_junit.Type type = pair.getFirst();
            String name = pair.getSecond();
            LocalVariableGen lv = mg.addLocalVariable(name, typeOf(type), null, null);
            locals.put(name, lv);
        }

        for (Statement statement : testMethod.getStatements()) {
            visitStatement(statement, locals, il, factory, cp);
        }

        il.append(InstructionConstants.RETURN);

        mg.setMaxStack();
        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    private void visitStatement(
            Statement statement,
            Map<String, LocalVariableGen> locals,
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

    static Type typeOf(yokohama.unit.ast_junit.Type type) {
        int dims = type.getDims();
        if (dims == 0) {
            return type.getNonArrayType().accept(
                    primitiveType -> {
                        Kind kind = primitiveType.getKind();
                        switch (kind) {
                            case BOOLEAN: return Type.BOOLEAN;
                            case BYTE:    return Type.BYTE;
                            case SHORT:   return Type.SHORT;
                            case INT:     return Type.INT;
                            case LONG:    return Type.LONG;
                            case CHAR:    return Type.CHAR;
                            case FLOAT:   return Type.FLOAT;
                            case DOUBLE:  return Type.DOUBLE;
                        }
                        throw new RuntimeException("should not reach here");
                    },
                    classType -> new ObjectType(classType.getText()));
        } else {
            return new ArrayType(
                    typeOf(new yokohama.unit.ast_junit.Type(type.getNonArrayType() , 0)),
                    dims);
        }
    }
}
