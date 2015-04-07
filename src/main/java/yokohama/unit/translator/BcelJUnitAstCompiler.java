package yokohama.unit.translator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.AnnotationEntryGen;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.apache.commons.collections4.ListUtils;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarDeclVisitor;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.util.Pair;

public class BcelJUnitAstCompiler implements JUnitAstCompiler {
    public static class CaughtExceptionVarVisitor {
        public static List<Pair<yokohama.unit.ast_junit.Type, String>> sortedSet(Stream<Pair<yokohama.unit.ast_junit.Type, String>> i) {
            return i.collect(Collectors.toSet())
                    .stream()
                    .sorted((o1, o2) -> o1.getSecond().compareTo(o2.getSecond()))
                    .collect(Collectors.toList());
        }

        public Stream<Pair<yokohama.unit.ast_junit.Type, String>> visitTestMethod(TestMethod testMethod) {
            return visitStatements(testMethod.getStatements());
        }

        public Stream<Pair<yokohama.unit.ast_junit.Type, String>> visitStatements(List<Statement> statements) {
            return statements.stream().flatMap(this::visitStatement);
        }

        public Stream<Pair<yokohama.unit.ast_junit.Type, String>> visitStatement(Statement statement) {
            return statement.accept(
                    isStatement -> Stream.<Pair<yokohama.unit.ast_junit.Type, String>>empty(),
                    isNotStatement -> Stream.<Pair<yokohama.unit.ast_junit.Type, String>>empty(),
                    varInitStatement -> Stream.<Pair<yokohama.unit.ast_junit.Type, String>>empty(),
                    tryStatement ->
                            Stream.concat(
                                    visitStatements(tryStatement.getTryStatements()),
                                    Stream.concat(
                                            tryStatement.getCatchClauses().stream().flatMap(this::visitCatchClause),
                                            visitStatements(tryStatement.getFinallyStatements()))),
                    ifStatement ->
                            Stream.concat(
                                    visitStatements(ifStatement.getThen()),
                                    visitStatements(ifStatement.getOtherwise())));
        }

        public Stream<Pair<yokohama.unit.ast_junit.Type, String>> visitCatchClause(CatchClause catchClause) {
            return Stream.concat(
                    Stream.of(
                            new Pair<>(
                                    catchClause.getClassType().toType(),
                                    catchClause.getVar().getName())),
                    visitStatements(catchClause.getStatements()));
        }
    }

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
        // set class file version to Java 1.5
        cg.setMajor(49);
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
        List<Pair<yokohama.unit.ast_junit.Type, String>> varDecls =
                VarDeclVisitor.sortedSet(new VarDeclVisitor().visitTestMethod(testMethod));
        List<Pair<yokohama.unit.ast_junit.Type, String>> caughtExVars =
                CaughtExceptionVarVisitor.sortedSet(new CaughtExceptionVarVisitor().visitTestMethod(testMethod));
        for (Pair<yokohama.unit.ast_junit.Type,String> pair :
                ListUtils.union(varDecls, caughtExVars)) {
            yokohama.unit.ast_junit.Type type = pair.getFirst();
            String name = pair.getSecond();
            if (locals.containsKey(name))
                throw new RuntimeException("duplicate local variable: " + name);
            LocalVariableGen lv = mg.addLocalVariable(name, typeOf(type), null, null);
            locals.put(name, lv);
        }

        for (Statement statement : testMethod.getStatements()) {
            visitStatement(statement, locals, mg, il, factory, cp);
        }

        il.append(InstructionConstants.RETURN);

        mg.setMaxStack();
        cg.addMethod(mg.getMethod());
        il.dispose();
    }

    private void visitStatement(
            Statement statement,
            Map<String, LocalVariableGen> locals,
            MethodGen mg,
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
            tryStatement -> {
                InstructionHandle startTry = il.append(InstructionFactory.NOP);
                for (Statement s : tryStatement.getTryStatements()) {
                    visitStatement(s, locals, mg, il, factory, cp);
                }
                InstructionHandle endTry = il.append(InstructionFactory.NOP);
                BranchInstruction goto_ = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
                il.append(goto_);

                List<BranchInstruction> catchExits = new ArrayList<>();
                for (CatchClause catchClause : tryStatement.getCatchClauses()) {
                    LocalVariableGen ex = locals.get(catchClause.getVar().getName());
                    InstructionHandle startCatch = il.append(
                            InstructionFactory.createStore(ex.getType(), ex.getIndex()));
                    mg.addExceptionHandler(
                            startTry,
                            endTry,
                            startCatch,
                            (ObjectType)typeOf(catchClause.getClassType().toType()));
                    for (Statement s : catchClause.getStatements()) {
                        this.visitStatement(s, locals, mg, il, factory, cp);
                    }
                    BranchInstruction exitCatch = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
                    il.append(exitCatch);
                    catchExits.add(exitCatch);
                }

                InstructionHandle startFinally = il.append(InstructionFactory.NOP);
                for (Statement s : tryStatement.getFinallyStatements()) {
                    visitStatement(s, locals, mg, il, factory, cp);
                }

                goto_.setTarget(startFinally);
                for (BranchInstruction bi : catchExits) {
                    bi.setTarget(startFinally);
                }

                return null;
            },
            ifStatement -> {
                LocalVariableGen lv = locals.get(ifStatement.getCond().getName());
                il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));

                // if
                BranchInstruction ifeq  = InstructionFactory.createBranchInstruction(Constants.IFEQ, null);
                il.append(ifeq);

                // then
                for (Statement thenStatement : ifStatement.getThen()) {
                    visitStatement(thenStatement, locals, mg, il, factory, cp);
                }
                BranchInstruction goto_ = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
                il.append(goto_);

                // else
                InstructionHandle else_ = il.append(InstructionFactory.NOP);
                for (Statement elseStatement : ifStatement.getOtherwise()) {
                    visitStatement(elseStatement, locals, mg, il, factory, cp);
                }

                InstructionHandle fi = il.append(InstructionFactory.NOP);

                // tie the knot
                ifeq.setTarget(else_);
                goto_.setTarget(fi);

                return null;
            }
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
                        new Type[] { Type.OBJECT, new ObjectType("org.hamcrest.Matcher") },
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
                        new Type[] { Type.OBJECT, new ObjectType("org.hamcrest.Matcher") },
                        Constants.INVOKESTATIC));
    }

    @SneakyThrows(ClassNotFoundException.class)
    private void visitVarInitStatement(
            VarInitStatement varInitStatement,
            Map<String, LocalVariableGen> locals,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        LocalVariableGen var = locals.get(varInitStatement.getName());
        Type type = typeOf(varInitStatement.getType());
        Type fromType = varInitStatement.getValue().<Type>accept(
            varExpr -> {
                LocalVariableGen from = locals.get(varExpr.getName());
                il.append(InstructionFactory.createLoad(from.getType(), from.getIndex()));
                return from.getType();
            },
            instanceOfMatcherExpr -> {
                il.append(new PUSH(cp, new ObjectType(instanceOfMatcherExpr.getClassName())));
                il.append(factory.createInvoke(
                        "org.hamcrest.CoreMatchers",
                        "instanceOf",
                        new ObjectType("org.hamcrest.Matcher"),
                        new Type[] { new ObjectType("java.lang.Class") },
                        Constants.INVOKESTATIC));
                return new ObjectType("org.hamcrest.Matcher");
            },
            nullValueMatcherExpr -> {
                il.append(factory.createInvoke(
                        "org.hamcrest.CoreMatchers",
                        "nullValue",
                        new ObjectType("org.hamcrest.Matcher"),
                        Type.NO_ARGS,
                        Constants.INVOKESTATIC));
                return new ObjectType("org.hamcrest.Matcher");
            },
            equalToMatcherExpr -> {
                Var operand = equalToMatcherExpr.getOperand();
                LocalVariableGen lv = locals.get(operand.getName());
                il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
                il.append(factory.createInvoke(
                        "org.hamcrest.CoreMatchers",
                        "is",
                        new ObjectType("org.hamcrest.Matcher"),
                        new Type[] { lv.getType() },
                        Constants.INVOKESTATIC));
                return new ObjectType("org.hamcrest.Matcher");
            },
            newExpr -> {
                il.append(factory.createNew(newExpr.getType()));
                il.append(InstructionConstants.DUP);
                il.append(factory.createInvoke(
                        newExpr.getType(),
                        "<init>",
                        Type.VOID,
                        Type.NO_ARGS,
                        Constants.INVOKESPECIAL));
                return new ObjectType(newExpr.getType());
            },
            strLitExpr -> {
                il.append(new PUSH(cp, strLitExpr.getText()));
                return Type.STRING;
            },
            nullExpr -> {
                il.append(InstructionConstants.ACONST_NULL);
                return Type.NULL;
            },
            invokeExpr -> {
                Type returnType = typeOf(invokeExpr.getReturnType());

                // first push target object
                LocalVariableGen object = locals.get(invokeExpr.getObject().getName());
                il.append(InstructionFactory.createLoad(object.getType(), object.getIndex()));
                // push arguments
                for (Var arg : invokeExpr.getArgs()) {
                    LocalVariableGen lv = locals.get(arg.getName());
                    il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
                }
                // then call method
                il.append(factory.createInvoke(
                        object.getType().toString(), // TODO: ?
                        invokeExpr.getMethodName(),
                        returnType,
                        invokeExpr.getArgTypes().stream()
                                .map(BcelJUnitAstCompiler::typeOf)
                                .collect(Collectors.toList())
                                .toArray(new Type[]{}),
                        invokeExpr.getInstruction() == InvokeExpr.Instruction.VIRTUAL
                                ? Constants.INVOKEVIRTUAL
                                : Constants.INVOKEINTERFACE));                

                return returnType;
            },
            invokeStaticExpr -> {
                Type returnType = typeOf(invokeStaticExpr.getReturnType());
                for (Var arg : invokeStaticExpr.getArgs()) {
                    LocalVariableGen lv = locals.get(arg.getName());
                    il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
                }
                il.append(factory.createInvoke(
                        invokeStaticExpr.getClazz().getText(),
                        invokeStaticExpr.getMethodName(),
                        returnType,
                        invokeStaticExpr.getArgTypes().stream()
                                .map(BcelJUnitAstCompiler::typeOf)
                                .collect(Collectors.toList())
                                .toArray(new Type[]{}),
                        Constants.INVOKESTATIC));
                return returnType;
            },
            intLitExpr -> {
                il.append(new PUSH(cp, intLitExpr.getValue()));
                return Type.INT;
            },
            classLitExpr -> {
                yokohama.unit.ast_junit.Type type_ = classLitExpr.getType();
                il.append(new PUSH(cp, new ObjectType(
                        type_.getDims() > 0
                                /* this is strange since BCEL has ArrayType apart from ObjectType,
                                   but there is no PUSH constructor in BCEL which takes ArrayType. */
                                ? type_.getFieldDescriptor()
                                : type_.getText())));
                return Type.CLASS;
            },
            equalOpExpr -> {
                LocalVariableGen lhs = locals.get(equalOpExpr.getLhs().getName());
                il.append(InstructionFactory.createLoad(lhs.getType(), lhs.getIndex()));
                LocalVariableGen rhs = locals.get(equalOpExpr.getRhs().getName());
                il.append(InstructionFactory.createLoad(rhs.getType(), rhs.getIndex()));

                // if
                BranchInstruction if_acmpne = InstructionFactory.createBranchInstruction(Constants.IF_ACMPNE, null);
                il.append(if_acmpne);
                // then
                il.append(new PUSH(cp, true));
                BranchInstruction goto_ = InstructionFactory.createBranchInstruction(Constants.GOTO, null);
                il.append(goto_);
                // else
                InstructionHandle else_ = il.append(new PUSH(cp, false));

                InstructionHandle endIf = il.append(InstructionFactory.NOP);

                // tie the knot
                if_acmpne.setTarget(else_);
                goto_.setTarget(endIf);

                return Type.BOOLEAN;
            });
        if (fromType instanceof ReferenceType && type instanceof ReferenceType) {
            ReferenceType fromType_ = (ReferenceType)fromType;
            ReferenceType type_ = (ReferenceType)type;
            if (!fromType_.isAssignmentCompatibleWith(type_)) {
                il.append(factory.createCheckCast(type_));
            }
        }
        il.append(InstructionFactory.createStore(var.getType(), var.getIndex()));
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
