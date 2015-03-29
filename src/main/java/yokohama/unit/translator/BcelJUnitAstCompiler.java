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
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.VarDeclVisitor;
import yokohama.unit.ast_junit.VarInitStatement;
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
            isStatement -> {
                visitIsStatement(isStatement, locals, il, factory, cp);
                return null;
            },
            isNotStatement -> {
                visitIsNotStatement(isNotStatement, locals, il, factory, cp);
                return null;
            },
            varInitStatement -> {
                visitVarInitStatement(varInitStatement, locals, il, factory, cp);
                return null;
            },
            returnIsStatement -> { return null; },
            returnIsNotStatement -> { return null; },
            invokeVoidStatement -> { return null; },
            tryStatement -> { return null; },
            ifStatement -> { return null; }
        );
    }

    private void visitIsStatement(
            IsStatement isStatement,
            Map<String, LocalVariableGen> locals,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        LocalVariableGen subject = locals.get(isStatement.getSubject().getName());
        LocalVariableGen complement = locals.get(isStatement.getComplement().getName());
        il.append(InstructionFactory.createLoad(subject.getType(), subject.getIndex()));
        il.append(InstructionFactory.createLoad(complement.getType(), complement.getIndex()));
        il.append(
                factory.createInvoke(
                        "org.junit.Assert",
                        "assertThat",
                        Type.VOID,
                        new Type[] { subject.getType(), complement.getType() },
                        Constants.INVOKESTATIC));
    }

    private void visitIsNotStatement(
            IsNotStatement isNotStatement,
            Map<String, LocalVariableGen> locals,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        LocalVariableGen subject = locals.get(isNotStatement.getSubject().getName());
        LocalVariableGen complement = locals.get(isNotStatement.getComplement().getName());
        il.append(InstructionFactory.createLoad(subject.getType(), subject.getIndex()));
        il.append(InstructionFactory.createLoad(complement.getType(), complement.getIndex()));
        il.append(
                factory.createInvoke(
                        "org.hamcrest.CoreMatchers",
                        "not",
                        new ObjectType("org.hamcrest.Matcher"),
                        new Type[] { complement.getType() },
                        Constants.INVOKESTATIC));
        il.append(
                factory.createInvoke(
                        "org.junit.Assert",
                        "assertThat",
                        Type.VOID,
                        new Type[] { subject.getType(), new ObjectType("org.hamcrest.Matcher") },
                        Constants.INVOKESTATIC));
    }

    private void visitVarInitStatement(
            VarInitStatement varInitStatement,
            Map<String, LocalVariableGen> locals,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        LocalVariableGen var = locals.get(varInitStatement.getName());
        Type type = typeOf(varInitStatement.getType());
        varInitStatement.getValue().<Void>accept(
            varExpr -> {
                LocalVariableGen from = locals.get(varExpr.getName());
                il.append(InstructionFactory.createLoad(from.getType(), from.getIndex()));
                il.append(InstructionFactory.createStore(var.getType(), var.getIndex()));
                return null;
            },
            matcherExpr -> { return null; },
            newExpr -> {
                il.append(factory.createNew(newExpr.getType()));
                il.append(InstructionConstants.DUP);
                il.append(factory.createInvoke(
                        newExpr.getType(),
                        "<init>",
                        Type.VOID,
                        Type.NO_ARGS,
                        Constants.INVOKESPECIAL));
                il.append(InstructionFactory.createStore(var.getType(), var.getIndex()));
                return null;
            },
            strLitExpr -> {
                il.append(new PUSH(cp, strLitExpr.getText()));
                il.append(InstructionFactory.createStore(var.getType(), var.getIndex()));
                return null;
            },
            nullExpr -> {
                il.append(InstructionConstants.ACONST_NULL);
                il.append(InstructionFactory.createStore(var.getType(), var.getIndex()));
                return null;
            },
            invokeExpr -> { return null; },
            invokeStaticExpr -> { return null; },
            intLitExpr -> { return null; },
            classLitExpr -> {
                il.append(new PUSH(cp, new ObjectType(classLitExpr.getType().getText())));
                il.append(InstructionFactory.createStore(var.getType(), var.getIndex()));
                return null;
            },
            equalOpExp -> { return null; });
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
