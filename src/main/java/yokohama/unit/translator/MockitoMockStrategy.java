package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast.MethodPattern;
import yokohama.unit.ast.StubBehavior;
import yokohama.unit.ast_junit.ClassLitExpr;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.IntLitExpr;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.PrimitiveType;
import yokohama.unit.ast_junit.Span;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;

public class MockitoMockStrategy implements MockStrategy {
    private static final ClassType MOCKITO = new ClassType("org.mockito.Mockito", Span.dummySpan());

    @Override
    public Stream<Statement> stub(
            String varName,
            yokohama.unit.ast.ClassType classToStub,
            List<StubBehavior> behavior,
            ExpressionStrategy expressionStrategy,
            String envVarName,
            GenSym genSym,
            Optional<Path> docyPath,
            String className,
            String packageName) {
        Stream<Statement> createMock = createMock(varName, classToStub, genSym, docyPath, className, packageName);
        Stream<Statement> defineBehavior = behavior.stream().flatMap(
                b -> defineBehavior(
                        varName,
                        b,
                        expressionStrategy,
                        envVarName,
                        genSym,
                        docyPath,
                        className,
                        packageName));
        return Stream.concat(createMock, defineBehavior);
    }

    private Stream<Statement> createMock(
            String varName,
            yokohama.unit.ast.ClassType classToStub,
            GenSym genSym,
            Optional<Path> docyPath,
            String className,
            String packageName) {
        Var classToStubVar = new Var(genSym.generate("classToStub"));
        Var fileNameVar = new Var(genSym.generate("fileName"));
        Var lineVar = new Var(genSym.generate("line"));
        Var spanVar = new Var(genSym.generate("span"));
        Span span = new Span(
                docyPath,
                classToStub.getSpan().getStart(),
                classToStub.getSpan().getEnd());
        Type clazz = new Type(new ClassType(classToStub.getName(), span), 0);
        return Stream.of(
                new VarInitStatement(Type.CLASS, classToStubVar.getName(),
                        new ClassLitExpr(clazz), Span.dummySpan()),
                new VarInitStatement(Type.STRING, fileNameVar.getName(),
                        new StrLitExpr(span.getFileName()), Span.dummySpan()),
                new VarInitStatement(Type.INT, lineVar.getName(),
                        new IntLitExpr(span.getStart().getLine()), Span.dummySpan()),
                new VarInitStatement(Type.STRING, spanVar.getName(),
                        new StrLitExpr(span.toString()), Span.dummySpan()),
                new VarInitStatement(clazz, varName,
                        new InvokeStaticExpr(
                                new ClassType(packageName + "." + className, Span.dummySpan()),
                                Arrays.asList(new Type(new ClassType(classToStub.getName(), Span.dummySpan()), 0)),
                                "mock_",
                                Arrays.asList(
                                        classToStubVar,
                                        fileNameVar,
                                        lineVar,
                                        spanVar)),
                       span));
    }

    private Stream<Statement> defineBehavior(
            String varName,
            StubBehavior behavior,
            ExpressionStrategy expressionStrategy,
            String envVarName,
            GenSym genSym,
            Optional<Path> docyPath,
            String className,
            String packageName) {
        Span span = new Span(
                docyPath,
                behavior.getSpan().getStart(),
                behavior.getSpan().getEnd());
        MethodPattern methodPattern = behavior.getMethodPattern();
        String methodName = methodPattern.getName();
        boolean isVarArg = methodPattern.isVarArg();
        List<yokohama.unit.ast.Type> argumentTypes = methodPattern.getArgumentTypes();

        String returnedVarName = genSym.generate("returned");
        Stream<Statement> returned = behavior.getToBeReturned().accept(
                quotedExpr ->
                        expressionStrategy.eval(
                                returnedVarName,
                                envVarName,
                                quotedExpr,
                                genSym,
                                docyPath,
                                className,
                                packageName).stream(),
                stubExpr->
                        this.stub(
                                returnedVarName,
                                stubExpr.getClassToStub(),
                                stubExpr.getBehavior(),
                                expressionStrategy,
                                envVarName,
                                genSym,
                                docyPath,
                                className,
                                packageName)
                );

        Stream<Var> argVars;
        Stream<Statement> argMatchers;
        if (isVarArg) {
            String varArg = genSym.generate("varArg");
            List<Pair<Var, Stream<Statement>>> pairs = argumentTypes.subList(0, argumentTypes.size() - 1).stream()
                    .map(argumentType -> mapArgumentType(argumentType, genSym, docyPath))
                    .collect(Collectors.toList());
            argVars = Stream.concat(
                    pairs.stream().map(Pair::getFirst),
                    Stream.of(new Var(varArg)));
            argMatchers = Stream.concat(
                    pairs.stream().flatMap(Pair::getSecond),
                    Stream.<Statement>of(
                            new VarInitStatement(
                                    Type.of(argumentTypes.get(argumentTypes.size() - 1)).toArray(),
                                    varArg,
                                    new InvokeStaticExpr(
                                            MOCKITO,
                                            Arrays.asList(
                                                    Type.of(argumentTypes.get(argumentTypes.size() - 1)).toArray()),
                                            "anyVararg", Arrays.asList()),
                                    span)));
        } else {
            List<Pair<Var, Stream<Statement>>> pairs = argumentTypes.stream()
                    .map(argumentType -> mapArgumentType(argumentType, genSym, docyPath))
                    .collect(Collectors.toList());
            argVars = pairs.stream().map(Pair::getFirst);
            argMatchers = pairs.stream().flatMap(Pair::getSecond);
        }

        // when ... thenReturn
        String invokeVarName = genSym.generate("invoke");
        String stubbingVarName = genSym.generate("stubbing");
        String __ = genSym.generate("__");
        Stream<Statement> whenReturn = Stream.of(
                new VarInitStatement(Type.OBJECT, invokeVarName, 
                        new InvokeExpr(
                                new Var(varName),
                                methodName,
                                argVars.collect(Collectors.toList())),
                        span),
                new VarInitStatement(
                        new Type(new ClassType("org.mockito.stubbing.OngoingStubbing", Span.dummySpan()), 0),
                        stubbingVarName,
                        new InvokeStaticExpr(
                                MOCKITO,
                                Arrays.asList(),
                                "when",
                                Arrays.asList(new Var(invokeVarName))),
                        span),
                new VarInitStatement(Type.OBJECT, __, 
                        new InvokeExpr(
                                new Var(stubbingVarName),
                                "thenReturn",
                                Arrays.asList(new Var(returnedVarName))),
                        span));

        return Stream.concat(
                returned,
                Stream.concat(argMatchers, whenReturn));
    }

    private Pair<Var, Stream<Statement>> mapArgumentType(
            yokohama.unit.ast.Type argumentType,
            GenSym genSym,
            Optional<Path> docyPath) {
        Span span = new Span(
                docyPath,
                argumentType.getSpan().getStart(),
                argumentType.getSpan().getEnd());
        String argVarName = genSym.generate("arg");
        int dims = argumentType.getDims();
        Stream<Statement> statements = argumentType.getNonArrayType().accept(
                primitiveType -> {
                    if (dims == 0) {
                        switch (primitiveType.getKind()) {
                            case BOOLEAN:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.BOOLEAN, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO, Arrays.asList(), "anyBoolean", Arrays.asList()),
                                                span));
                            case BYTE:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.BYTE, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO, Arrays.asList(), "anyByte", Arrays.asList()),
                                                span));
                            case SHORT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.SHORT, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO, Arrays.asList(), "anyShort", Arrays.asList()),
                                                span));
                            case INT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.INT, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO, Arrays.asList(), "anyInt", Arrays.asList()),
                                                span));
                            case LONG:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.LONG, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO, Arrays.asList(), "anyLong", Arrays.asList()),
                                                span));
                            case CHAR:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.CHAR, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO, Arrays.asList(), "anyChar", Arrays.asList()),
                                                span));
                            case FLOAT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.FLOAT, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO, Arrays.asList(), "anyFloat", Arrays.asList()),
                                                span));
                            case DOUBLE:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.DOUBLE, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO, Arrays.asList(), "anyDouble", Arrays.asList()),
                                                span));
                            default:
                                throw new RuntimeException("should not reach here");
                        }
                    } else {
                        String clazzVarName = genSym.generate("clazz");
                        Type type;
                        switch (primitiveType.getKind()) {
                            case BOOLEAN: type = new Type(new PrimitiveType(Kind.BOOLEAN), dims); break;
                            case BYTE:    type = new Type(new PrimitiveType(Kind.BYTE),    dims); break;
                            case SHORT:   type = new Type(new PrimitiveType(Kind.SHORT),   dims); break;
                            case INT:     type = new Type(new PrimitiveType(Kind.INT),     dims); break;
                            case LONG:    type = new Type(new PrimitiveType(Kind.LONG),    dims); break;
                            case CHAR:    type = new Type(new PrimitiveType(Kind.CHAR),    dims); break;
                            case FLOAT:   type = new Type(new PrimitiveType(Kind.FLOAT),   dims); break;
                            case DOUBLE:  type = new Type(new PrimitiveType(Kind.DOUBLE),  dims); break;
                            default: throw new RuntimeException("should not reach here");
                        }
                        return Stream.<Statement>of(
                                new VarInitStatement(Type.CLASS, clazzVarName, new ClassLitExpr(type), span),
                                new VarInitStatement(type, argVarName,
                                        new InvokeStaticExpr(
                                                MOCKITO,
                                                Arrays.asList(type),
                                                "isA",
                                                Arrays.asList(new Var(clazzVarName))),
                                        Span.dummySpan()));
                    }
                },
                classType -> {
                    String clazzVarName = genSym.generate("clazz");
                    Type type = new Type(new ClassType(classType.getName(), Span.dummySpan()), dims);
                    return Stream.<Statement>of(
                            new VarInitStatement(Type.CLASS, clazzVarName, new ClassLitExpr(type), span),
                            new VarInitStatement(type, argVarName,
                                    new InvokeStaticExpr(
                                            MOCKITO,
                                            Arrays.asList(type),
                                            "isA",
                                            Arrays.asList(new Var(clazzVarName))),
                                    Span.dummySpan()));
                });
        return new Pair<>(new Var(argVarName), statements);
    }
}
