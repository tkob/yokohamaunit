package yokohama.unit.translator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast.MethodPattern;
import yokohama.unit.position.Span;
import yokohama.unit.ast.StubBehavior;
import yokohama.unit.ast.StubExpr;
import yokohama.unit.ast_junit.ClassLitExpr;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.PrimitiveType;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;

@AllArgsConstructor
public class MockitoMockStrategy implements MockStrategy {
    private final String name;
    private final String packageName;
    private final GenSym genSym;

    private static final ClassType MOCKITO = new ClassType("org.mockito.Mockito", Span.dummySpan());

    @Override
    public List<Statement> stub(
            String varName,
            StubExpr stubExpr,
            ExpressionStrategy expressionStrategy,
            String envVarName,
            ClassResolver classResolver) {
        String classToStubName =      stubExpr.getClassToStub().getCanonicalName(classResolver);
        Span classToStubSpan =        stubExpr.getSpan();
        List<StubBehavior> behavior = stubExpr.getBehavior();

        /*
          Create a mock first, and then define "when...then" behavior.
        */
        Stream<Statement> createMock = createMock(varName, classToStubName, classToStubSpan);
        Stream<Statement> defineBehavior = behavior.stream().flatMap(
                b -> defineBehavior(
                        varName,
                        classToStubName,
                        classToStubSpan,
                        b,
                        expressionStrategy,
                        envVarName,
                        classResolver));
        return Stream.concat(createMock, defineBehavior).collect(Collectors.toList());
    }

    private Stream<Statement> createMock(
            String varName,
            String classToStubName,
            Span classToStubSpan) {
        // Call Mockito.mock method with the class and bind the variable to the result.
        Var classToStubVar = new Var(genSym.generate("classToStub"));
        Type clazz = new Type(new ClassType(classToStubName, classToStubSpan), 0);
        return Stream.of(
                new VarInitStatement(Type.CLASS, classToStubVar.getName(),
                        new ClassLitExpr(clazz), Span.dummySpan()),
                new VarInitStatement(clazz, varName,
                        new InvokeStaticExpr(
                                MOCKITO,
                                Arrays.asList(new Type(new ClassType(classToStubName, classToStubSpan), 0)),
                                "mock",
                                Arrays.asList(Type.CLASS),
                                Arrays.asList(classToStubVar),
                                Type.OBJECT),
                       classToStubSpan));
    }

    private Stream<Statement> defineBehavior(
            String varName,
            String classToStubName,
            Span classToStubSpan,
            StubBehavior behavior,
            ExpressionStrategy expressionStrategy,
            String envVarName,
            ClassResolver classResolver) {
        /*
        Defining behavior consists of four parts:
        1. Define value to return when the stub method is called (`returned`)
        2. Invoke the method with appropriate matchers
           2a. Prepare matchers (`argMatchers`)
           2b. Invoke the method with the matchers (`invoke`)
        3. Tell Mockito the return value (`whenReturn`)
        */

        Span span = behavior.getSpan();
        MethodPattern methodPattern = behavior.getMethodPattern();
        String methodName = methodPattern.getName();
        boolean isVarArg = methodPattern.isVarArg();
        List<yokohama.unit.ast.Type> argumentTypes = methodPattern.getArgumentTypes();
        Type returnType = getReturnType(classToStubName, methodName, argumentTypes, isVarArg, classResolver);

        String returnedVarName = genSym.generate("returned");
        Stream<Statement> returned = behavior.getToBeReturned().accept(
                quotedExpr ->
                        expressionStrategy.eval(
                                returnedVarName,
                                envVarName,
                                quotedExpr).stream(),
                stubExpr->
                        this.stub(
                                returnedVarName,
                                stubExpr,
                                expressionStrategy,
                                envVarName,
                                classResolver).stream());

        Stream<Type> argTypes;
        Stream<Var> argVars;
        Stream<Statement> argMatchers;
        if (isVarArg) {
            String varArg = genSym.generate("varArg");
            List<Pair<Var, Stream<Statement>>> pairs = argumentTypes.subList(0, argumentTypes.size() - 1).stream()
                    .map(argumentType -> mapArgumentType(argumentType, classResolver))
                    .collect(Collectors.toList());
            argTypes = Stream.concat(
                    argumentTypes.subList(0, argumentTypes.size() - 1)
                            .stream()
                            .map(type -> Type.of(type, classResolver)),
                    Stream.of(
                            Type.of(argumentTypes.get(argumentTypes.size() - 1), classResolver).toArray()));
            argVars = Stream.concat(
                    pairs.stream().map(Pair::getFirst),
                    Stream.of(new Var(varArg)));
            argMatchers = Stream.concat(
                    pairs.stream().flatMap(Pair::getSecond),
                    Stream.<Statement>of(
                            new VarInitStatement(
                                    Type.of(argumentTypes.get(argumentTypes.size() - 1), classResolver).toArray(),
                                    varArg,
                                    new InvokeStaticExpr(
                                            MOCKITO,
                                            Arrays.asList(
                                                    Type.of(argumentTypes.get(argumentTypes.size() - 1), classResolver).toArray()),
                                            "anyVararg",
                                            Arrays.asList(),
                                            Arrays.asList(),
                                            Type.OBJECT),
                                    span)));
        } else {
            List<Pair<Var, Stream<Statement>>> pairs = argumentTypes.stream()
                    .map(argumentType -> mapArgumentType(argumentType, classResolver))
                    .collect(Collectors.toList());
            argTypes = methodPattern.getArgumentTypes().stream().map(type -> Type.of(type, classResolver));
            argVars = pairs.stream().map(Pair::getFirst);
            argMatchers = pairs.stream().flatMap(Pair::getSecond);
        }

        // invoke the method
        String invokeVarName = genSym.generate("invoke");
        String invokeTmpVarName = returnType.isPrimitive() ? genSym.generate("invoke") : invokeVarName;
        Stream<Statement> invoke = Stream.concat(
                Stream.of(
                        new VarInitStatement(returnType, invokeTmpVarName, 
                                new InvokeExpr(
                                        isInterface(classToStubName)
                                                ? InvokeExpr.Instruction.INTERFACE
                                                : InvokeExpr.Instruction.VIRTUAL,
                                        new Var(varName),
                                        methodName,
                                        argTypes.collect(Collectors.toList()),
                                        argVars.collect(Collectors.toList()),
                                        returnType),
                                span)),
                // box primitive type if needed
                returnType.getDims() == 0
                        ? returnType.getNonArrayType().accept(
                                primitiveType -> {
                                    ClassType boxed = primitiveType.box();
                                    return Stream.of(
                                            new VarInitStatement(boxed.toType(), invokeVarName,
                                                    new InvokeStaticExpr(
                                                            boxed,
                                                            Arrays.asList(),
                                                            "valueOf",
                                                            Arrays.asList(primitiveType.toType()),
                                                            Arrays.asList(new Var(invokeTmpVarName)),
                                                            boxed.toType()),
                                                    span));
                                },
                                classType -> Stream.empty())
                        : Stream.empty());

        // when ... thenReturn
        String stubbingVarName = genSym.generate("stubbing");
        String __ = genSym.generate("__");
        Stream<Statement> whenReturn = Stream.of(
                new VarInitStatement(
                        new Type(new ClassType("org.mockito.stubbing.OngoingStubbing", Span.dummySpan()), 0),
                        stubbingVarName,
                        new InvokeStaticExpr(
                                MOCKITO,
                                Arrays.asList(),
                                "when",
                                Arrays.asList(Type.OBJECT),
                                Arrays.asList(new Var(invokeVarName)),
                                new Type(new ClassType("org.mockito.stubbing.OngoingStubbing", Span.dummySpan()), 0)),
                        span),
                new VarInitStatement(Type.OBJECT, __, 
                        new InvokeExpr(
                                InvokeExpr.Instruction.INTERFACE,
                                new Var(stubbingVarName),
                                "thenReturn",
                                Arrays.asList(Type.OBJECT),
                                Arrays.asList(new Var(returnedVarName)),
                                new Type(new ClassType("org.mockito.stubbing.OngoingStubbing", Span.dummySpan()), 0)),
                        span));

        return Stream.concat(returned,
                Stream.concat(argMatchers,
                        Stream.concat(invoke, whenReturn)));
    }

    private Pair<Var, Stream<Statement>> mapArgumentType(
            yokohama.unit.ast.Type argumentType,
            ClassResolver classResolver) {
        Span span = argumentType.getSpan();
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
                                                        MOCKITO,
                                                        Arrays.asList(),
                                                        "anyBoolean",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.BOOLEAN),
                                                span));
                            case BYTE:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.BYTE, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO,
                                                        Arrays.asList(),
                                                        "anyByte",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.BYTE),
                                                span));
                            case SHORT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.SHORT, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO,
                                                        Arrays.asList(),
                                                        "anyShort",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.SHORT),
                                                span));
                            case INT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.INT, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO,
                                                        Arrays.asList(),
                                                        "anyInt",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.INT),
                                                span));
                            case LONG:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.LONG, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO,
                                                        Arrays.asList(),
                                                        "anyLong",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.LONG),
                                                span));
                            case CHAR:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.CHAR, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO,
                                                        Arrays.asList(),
                                                        "anyChar",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.CHAR),
                                                span));
                            case FLOAT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.FLOAT, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO,
                                                        Arrays.asList(),
                                                        "anyFloat",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.FLOAT),
                                                span));
                            case DOUBLE:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.DOUBLE, argVarName,
                                                new InvokeStaticExpr(
                                                        MOCKITO,
                                                        Arrays.asList(),
                                                        "anyDouble",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.DOUBLE),
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
                                                Arrays.asList(Type.CLASS),
                                                Arrays.asList(new Var(clazzVarName)),
                                                Type.OBJECT),
                                        Span.dummySpan()));
                    }
                },
                classType -> {
                    String clazzVarName = genSym.generate("clazz");
                    Type type = new Type(new ClassType(classType.getCanonicalName(classResolver), Span.dummySpan()), dims);
                    return Stream.<Statement>of(
                            new VarInitStatement(Type.CLASS, clazzVarName, new ClassLitExpr(type), span),
                            new VarInitStatement(type, argVarName,
                                    new InvokeStaticExpr(
                                            MOCKITO,
                                            Arrays.asList(type),
                                            "isA",
                                            Arrays.asList(Type.CLASS),
                                            Arrays.asList(new Var(clazzVarName)),
                                            Type.OBJECT),
                                    Span.dummySpan()));
                });
        return new Pair<>(new Var(argVarName), statements);
    }

    @SneakyThrows
    private Type getReturnType(
            String classToStubName,
            String methodName,
            List<yokohama.unit.ast.Type> argumentTypes,
            boolean isVarArg,
            ClassResolver classResolver) {
        Class<?> clazz = Class.forName(classToStubName);
        Method method = clazz.getMethod(
                methodName,
                (isVarArg
                        ? ListUtils.union(
                                argumentTypes.subList(0, argumentTypes.size() - 1),
                                Arrays.asList(
                                        argumentTypes.get(argumentTypes.size() - 1).toArray())) 
                        : argumentTypes).stream()
                        .map(type -> Type.of(type, classResolver))
                        .map(Type::toClass)
                        .collect(Collectors.toList())
                        .toArray(new Class[]{}));
        return Type.fromClass(method.getReturnType());
    }

    @SneakyThrows(ClassNotFoundException.class)
    private boolean isInterface(String className) {
        return Class.forName(className).isInterface();
    }
}
