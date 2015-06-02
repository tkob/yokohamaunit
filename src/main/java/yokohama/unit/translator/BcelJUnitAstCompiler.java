package yokohama.unit.translator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
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
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;
import org.apache.commons.collections4.ListUtils;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast_junit.Annotation;
import yokohama.unit.ast_junit.ArrayExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.InvokeStaticVoidStatement;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.Method;
import yokohama.unit.ast_junit.ReturnStatement;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.util.Sym;
import yokohama.unit.ast_junit.VarDeclVisitor;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;
import yokohama.unit.util.Pair;

public class BcelJUnitAstCompiler implements JUnitAstCompiler {
    public static class CaughtExceptionVarVisitor {
        public static List<Pair<yokohama.unit.ast_junit.Type, String>> sortedSet(Stream<Pair<yokohama.unit.ast_junit.Type, String>> i) {
            return i.collect(Collectors.toSet())
                    .stream()
                    .sorted((o1, o2) -> o1.getSecond().compareTo(o2.getSecond()))
                    .collect(Collectors.toList());
        }

        public Stream<Pair<yokohama.unit.ast_junit.Type, String>> visitTestMethod(Method method) {
            return visitStatements(method.getStatements());
        }

        public Stream<Pair<yokohama.unit.ast_junit.Type, String>> visitStatements(List<Statement> statements) {
            return statements.stream().flatMap(this::visitStatement);
        }

        public Stream<Pair<yokohama.unit.ast_junit.Type, String>> visitStatement(Statement statement) {
            return statement.accept(
                    isStatement -> Stream.<Pair<yokohama.unit.ast_junit.Type, String>>empty(),
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
                                    visitStatements(ifStatement.getOtherwise())),
                    returnStatement -> Stream.<Pair<yokohama.unit.ast_junit.Type, String>>empty(),
                    invokeVoidStatement -> Stream.<Pair<yokohama.unit.ast_junit.Type, String>>empty(),
                    invokeStaticVoidStatement -> Stream.<Pair<yokohama.unit.ast_junit.Type, String>>empty());
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
    public List<ErrorMessage> compile(
            Path docyPath,
            CompilationUnit ast,
            String className,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            List<String> javacArgs) {
        String joinedCp =
                classPath.stream().collect(Collectors.joining(File.pathSeparator));
        Repository.setRepository(
                SyntheticRepository.getInstance(new ClassPath(joinedCp)));

        for (ClassDecl classDecl : ast.getClassDecls()) {
            Optional<ClassType> extended = classDecl.getExtended();
            List<ClassType> implemented = classDecl.getImplemented();
            ClassGen cg = new ClassGen(
                    packageName.equals("") ? classDecl.getName() : packageName + "." + classDecl.getName(),
                    extended.isPresent()
                            ? extended.get().getClass().getCanonicalName()
                            : "java.lang.Object",
                    docyPath.getFileName().toString(), // source file name
                    Constants.ACC_PUBLIC | Constants.ACC_SUPER,
                    implemented.stream()
                            .map(ClassType::getClazz)
                            .map(Class::getCanonicalName)
                            .collect(Collectors.toList())
                            .toArray(new String[]{}));

            // set class file version to Java 1.5
            cg.setMajor(49);
            cg.setMinor(0);

            ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
            cg.addEmptyConstructor(Constants.ACC_PUBLIC);

            for (Method method : classDecl.getMethods()) {
                visitTestMethod(method, cg, cp);
            }

            try {
                Path classFilePath =
                        TranslatorUtils.makeClassFilePath(dest, packageName, classDecl.getName(), ".class");
                cg.getJavaClass().dump(classFilePath.toFile());
                Repository.getRepository().storeClass(cg.getJavaClass());
            } catch(IOException e) {
                System.err.println(e);
            }
        }
        return Collections.emptyList();
    }

    private void visitTestMethod(Method method, ClassGen cg, ConstantPoolGen cp) {
        InstructionList il = new InstructionList();
        MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, // access flags
                method.getReturnType().isPresent()
                        ? typeOf(method.getReturnType().get())
                        : Type.VOID,
                method.getArgs().stream()
                        .map(Pair::getFirst)
                        .map(BcelJUnitAstCompiler::typeOf)
                        .collect(Collectors.toList()).toArray(new Type[]{}),
                method.getArgs().stream()
                        .map(Pair::getSecond)
                        .collect(Collectors.toList()).toArray(new String[]{}),
                method.getName(),
                cg.getClassName(),
                il, cp);

        for (Annotation annotation : method.getAnnotations())  {
            AnnotationEntryGen ag = new AnnotationEntryGen(
                    new ObjectType(annotation.getClazz().getText()),
                    Arrays.asList(),
                    true,
                    cp);
            mg.addAnnotationEntry(ag);
        }

        InstructionFactory factory = new InstructionFactory(cg);

        Map<String, LocalVariableGen> locals = new HashMap<>();
        List<Pair<yokohama.unit.ast_junit.Type, String>> args = method.getArgs();
        List<Pair<yokohama.unit.ast_junit.Type, String>> varDecls =
                VarDeclVisitor.sortedSet(new VarDeclVisitor().visitMethod(method));
        List<Pair<yokohama.unit.ast_junit.Type, String>> caughtExVars =
                CaughtExceptionVarVisitor.sortedSet(new CaughtExceptionVarVisitor().visitTestMethod(method));
        for (Pair<yokohama.unit.ast_junit.Type,String> pair :
                ListUtils.union(varDecls, caughtExVars)) {
            yokohama.unit.ast_junit.Type type = pair.getFirst();
            String name = pair.getSecond();
            if (locals.containsKey(name))
                throw new RuntimeException("duplicate local variable: " + name);
            LocalVariableGen lv = mg.addLocalVariable(name, typeOf(type), null, null);
            locals.put(name, lv);
        }
        // populate locals again, since method arguments are not there yet
        for (LocalVariableGen lv : mg.getLocalVariables()) {
            String name = lv.getName();
            locals.put(name, lv);
        }

        for (Statement statement : method.getStatements()) {
            visitStatement(statement, locals, mg, il, factory, cp);
        }

        if (!method.getReturnType().isPresent()) {
            il.append(InstructionConstants.RETURN);
        }

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
                visitIsStatement(isStatement, locals, mg, il, factory, cp);
                return null;
            },
            varInitStatement -> {
                visitVarInitStatement(varInitStatement, locals, mg, il, factory, cp);
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
            },
            returnStatement -> {
                visitReturnStatement(returnStatement, locals, il, factory, cp);
                return null;
            },
            invokeVoidStatement -> {
                visitInvokeVoidStatement(invokeVoidStatement, locals, mg, il, factory, cp);
                return null;
            },
            invokeStaticVoidStatement -> {
                visitInvokeStaticVoidStatement(invokeStaticVoidStatement, locals, mg, il, factory, cp);
                return null;
            });
    }

    private void visitIsStatement(
            IsStatement isStatement,
            Map<String, LocalVariableGen> locals,
            MethodGen mg,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        LocalVariableGen message = locals.get(isStatement.getMessage().getName());
        LocalVariableGen subject = locals.get(isStatement.getSubject().getName());
        LocalVariableGen complement = locals.get(isStatement.getComplement().getName());
        InstructionHandle ih =
                il.append(InstructionFactory.createLoad(message.getType(), message.getIndex()));
        addLineNumber(mg, ih, isStatement.getSpan());
        il.append(InstructionFactory.createLoad(subject.getType(), subject.getIndex()));
        il.append(InstructionFactory.createLoad(complement.getType(), complement.getIndex()));
        il.append(
                factory.createInvoke(
                        "org.junit.Assert",
                        "assertThat",
                        Type.VOID,
                        new Type[] { Type.STRING, Type.OBJECT, new ObjectType("org.hamcrest.Matcher") },
                        Constants.INVOKESTATIC));
    }

    @SneakyThrows(ClassNotFoundException.class)
    private void visitVarInitStatement(
            VarInitStatement varInitStatement,
            Map<String, LocalVariableGen> locals,
            MethodGen mg,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        InstructionHandle ih = il.append(InstructionFactory.NOP);
        addLineNumber(mg, ih, varInitStatement.getSpan());
        LocalVariableGen var = locals.get(varInitStatement.getVar().getName());
        Type type = typeOf(varInitStatement.getType());
        Type fromType = varInitStatement.getValue().<Type>accept(varExpr -> {
                LocalVariableGen from = locals.get(varExpr.getVar().getName());
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
                Sym operand = equalToMatcherExpr.getOperand();
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
            regExpMatcherExpr -> {
                il.append(new PUSH(cp, regExpMatcherExpr.getPattern()));
                il.append(factory.createInvoke(
                        "com.jcabi.matchers.RegexMatchers",
                        "containsPattern",
                        new ObjectType("org.hamcrest.Matcher"),
                        new Type[] { Type.STRING },
                        Constants.INVOKESTATIC));
                return new ObjectType("org.hamcrest.Matcher");
            },
            newExpr -> {
                il.append(factory.createNew(newExpr.getType()));
                il.append(InstructionConstants.DUP);
                // push arguments
                for (Sym arg : newExpr.getArgs()) {
                    LocalVariableGen lv = locals.get(arg.getName());
                    il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
                }
                il.append(factory.createInvoke(
                        newExpr.getType(),
                        "<init>",
                        Type.VOID,
                        newExpr.getArgTypes().stream()
                                .map(BcelJUnitAstCompiler::typeOf)
                                .collect(Collectors.toList())
                                .toArray(new Type[]{}),
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
                for (Sym arg : invokeExpr.getArgs()) {
                    LocalVariableGen lv = locals.get(arg.getName());
                    il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
                }
                // then call method
                il.append(factory.createInvoke(
                        invokeExpr.getClassType().getText(),
                        invokeExpr.getMethodName(),
                        returnType,
                        invokeExpr.getArgTypes().stream()
                                .map(BcelJUnitAstCompiler::typeOf)
                                .collect(Collectors.toList())
                                .toArray(new Type[]{}),
                        invokeExpr.getClassType().isInterface()
                                ? Constants.INVOKEINTERFACE                
                                : Constants.INVOKEVIRTUAL));

                return returnType;
            },
            invokeStaticExpr -> {
                Type returnType = typeOf(invokeStaticExpr.getReturnType());
                for (Sym arg : invokeStaticExpr.getArgs()) {
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
            longLitExpr -> {
                il.append(new PUSH(cp, longLitExpr.getValue()));
                return Type.LONG;
            },
            floatLitExpr -> {
                il.append(new PUSH(cp, floatLitExpr.getValue()));
                return Type.FLOAT;
            },
            doubleLitExpr -> {
                il.append(new PUSH(cp, doubleLitExpr.getValue()));
                return Type.DOUBLE;
            },
            booleanLitExpr -> {
                il.append(new PUSH(cp, booleanLitExpr.getValue()));
                return Type.BOOLEAN;
            },
            charLitExpr -> {
                il.append(new PUSH(cp, new Character(charLitExpr.getValue())));
                return Type.CHAR;
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
            },
            arrayExpr -> this.visitArrayExpr(arrayExpr, locals, il, factory, cp));
        if (fromType instanceof ReferenceType && type instanceof ReferenceType) {
            ReferenceType fromType_ = (ReferenceType)fromType;
            ReferenceType type_ = (ReferenceType)type;
            if (!fromType_.isAssignmentCompatibleWith(type_)) {
                il.append(factory.createCheckCast(type_));
            }
        }
        il.append(InstructionFactory.createStore(var.getType(), var.getIndex()));
    }

    private Type visitArrayExpr(
            ArrayExpr arrayExpr,
            Map<String, LocalVariableGen> locals,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        Type componentType = typeOf(arrayExpr.getType().getNonArrayType().toType());
        List<Sym> contents = arrayExpr.getContents();
        il.append(new PUSH(cp, contents.size()));
        il.append(factory.createNewArray(
                componentType,
                (short)arrayExpr.getType().getDims()));
        for (int i = 0; i < contents.size(); i++) {
            il.append(InstructionConstants.DUP);
            Sym content = contents.get(i);
            LocalVariableGen lv = locals.get(content.getName());
            il.append(new PUSH(cp, i));
            il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
            il.append(InstructionFactory.createArrayStore(componentType));
        }
        return typeOf(arrayExpr.getType());
    }

    private void visitReturnStatement(
            ReturnStatement returnStatement,
            Map<String, LocalVariableGen> locals,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        LocalVariableGen lv = locals.get(returnStatement.getReturned().getName());
        il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
        il.append(InstructionFactory.createReturn(lv.getType()));
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

    private void visitInvokeVoidStatement(
            InvokeVoidStatement invokeVoidStatement,
            Map<String, LocalVariableGen> locals,
            MethodGen mg,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        // first push target object
        LocalVariableGen object = locals.get(invokeVoidStatement.getObject().getName());
        InstructionHandle ih =
                il.append(InstructionFactory.createLoad(object.getType(), object.getIndex()));
        addLineNumber(mg, ih, invokeVoidStatement.getSpan());
        // push arguments
        for (Sym arg : invokeVoidStatement.getArgs()) {
            LocalVariableGen lv = locals.get(arg.getName());
            il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
        }
        // then call method
        il.append(factory.createInvoke(
                invokeVoidStatement.getClassType().getText(),
                invokeVoidStatement.getMethodName(),
                Type.VOID,
                invokeVoidStatement.getArgTypes().stream()
                        .map(BcelJUnitAstCompiler::typeOf)
                        .collect(Collectors.toList())
                        .toArray(new Type[]{}),
                invokeVoidStatement.getClassType().isInterface()
                        ? Constants.INVOKEINTERFACE
                        : Constants.INVOKEVIRTUAL));                
    }

    private void visitInvokeStaticVoidStatement(
            InvokeStaticVoidStatement invokeStaticVoidStatement,
            Map<String, LocalVariableGen> locals,
            MethodGen mg,
            InstructionList il,
            InstructionFactory factory,
            ConstantPoolGen cp) {
        InstructionHandle ih = il.append(InstructionFactory.NOP);
        addLineNumber(mg, ih, invokeStaticVoidStatement.getSpan());
        for (Sym arg : invokeStaticVoidStatement.getArgs()) {
            LocalVariableGen lv = locals.get(arg.getName());
            il.append(InstructionFactory.createLoad(lv.getType(), lv.getIndex()));
        }
        il.append(factory.createInvoke(
                invokeStaticVoidStatement.getClazz().getText(),
                invokeStaticVoidStatement.getMethodName(),
                Type.VOID,
                invokeStaticVoidStatement.getArgTypes().stream()
                        .map(BcelJUnitAstCompiler::typeOf)
                        .collect(Collectors.toList())
                        .toArray(new Type[]{}),
                Constants.INVOKESTATIC));
    }

    private void addLineNumber(MethodGen mg, InstructionHandle ih, Span span) {
        int line = span.getStart().getLine();
        if (line > -1) {
            mg.addLineNumber(ih, line);
        }
    }
}
