package yokohama.unit.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import yokohama.unit.ast.Kind;
import yokohama.unit.ast.MethodPattern;
import yokohama.unit.ast.StubBehavior;
import yokohama.unit.ast.StubExpr;
import yokohama.unit.ast.StubReturns;
import yokohama.unit.ast.StubThrows;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassLitExpr;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.PrimitiveType;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.util.Sym;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;

@AllArgsConstructor
public class MockitoMockStrategy implements MockStrategy {
    private final String name;
    private final String packageName;
    private final GenSym genSym;
    private final ClassResolver classResolver;

    static final String MOCKITO = "org.mockito.Mockito";
    static final String ONGOING_STUBBING = "org.mockito.stubbing.OngoingStubbing";

    @SneakyThrows(ClassNotFoundException.class)
    ClassType classTypeOf(String name) {
        return new ClassType(classResolver.lookup(name));
    }
    Type typeOf(String name) {
        return classTypeOf(name).toType();
    }

    @Override
    public Collection<ClassDecl> auxClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<Statement> stub(
            Sym var,
            StubExpr stubExpr,
            AstToJUnitAstVisitor astToJUnitAstVisitor,
            Sym envVar) {
        List<StubBehavior> behavior = stubExpr.getBehavior();

        /*
          Create a mock first, and then define "when...then" behavior.
        */
        Stream<Statement> createMock = createMock(var, stubExpr.getClassToStub(), classResolver);
        Stream<Statement> defineBehavior = behavior.stream().flatMap(
                b -> defineBehavior(
                        var,
                        stubExpr.getClassToStub(),
                        b,
                        astToJUnitAstVisitor,
                        envVar,
                        classResolver));
        return Stream.concat(createMock, defineBehavior).collect(Collectors.toList());
    }

    private Stream<Statement> createMock(
            Sym var,
            yokohama.unit.ast.ClassType classToStub,
            ClassResolver classResolver) {
        // Call Mockito.mock method with the class and bind the variable to the result.
        Sym classToStubVar = genSym.generate("classToStub");
        Type clazz = Type.of(classToStub.toType(), classResolver);
        return Stream.of(
                new VarInitStatement(Type.CLASS, classToStubVar,
                        new ClassLitExpr(clazz), classToStub.getSpan()),
                new VarInitStatement(clazz, var,
                        new InvokeStaticExpr(
                                classTypeOf(MOCKITO),
                                Arrays.asList(clazz),
                                "mock",
                                Arrays.asList(Type.CLASS),
                                Arrays.asList(classToStubVar),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    private Stream<Statement> defineBehavior(
            Sym var,
            yokohama.unit.ast.ClassType classToStub,
            StubBehavior behavior,
            AstToJUnitAstVisitor astToJUnitAstVisitor,
            Sym envVar,
            ClassResolver classResolver) {
        return behavior.accept(
                stubReturns ->
                        defineReturns(
                                var,
                                classToStub,
                                stubReturns,
                                astToJUnitAstVisitor,
                                envVar, classResolver),
                stubThrows -> { throw new UnsupportedOperationException(); });
    }

    private Stream<Statement> defineReturns(
            Sym var,
            yokohama.unit.ast.ClassType classToStub,
            StubReturns stubReturns,
            AstToJUnitAstVisitor astToJUnitAstVisitor,
            Sym envVar,
            ClassResolver classResolver) {
        /*
        Defining behavior consists of three parts:
        1. Define value to return when the stub method is called (`returned`)
        2. Invoke the method with appropriate matchers
        3. Tell Mockito the return value (`whenThenReturn`)
        */

        Span span = stubReturns.getSpan();
        MethodPattern methodPattern = stubReturns.getMethodPattern();
        String methodName = methodPattern.getName();
        boolean isVararg = methodPattern.isVararg();
        List<yokohama.unit.ast.Type> argumentTypes = methodPattern.getParamTypes();

        Type returnType =
                methodPattern.getReturnType(classToStub, classResolver)
                        .map(type -> Type.of(type, classResolver))
                        .get();

        Sym returnedVar = genSym.generate("returned");
        Stream<Statement> returned = astToJUnitAstVisitor.translateExpr(stubReturns.getToBeReturned(),
                returnedVar,
                returnType.box().toClass(),
                envVar);

        Sym invokeVar = genSym.generate("invoke");
        Stream<Statement> invokeWithMatchers = invokeWithMatchers(
                var,
                invokeVar,
                classToStub,
                returnType,
                methodPattern,
                span);

        // when ... thenReturn
        Stream<Statement> whenThenReturn =
                whenThenReturn(invokeVar, returnedVar, span);

        return Stream.concat(returned,
                Stream.concat(invokeWithMatchers, whenThenReturn));
    }

    private Stream<Statement> invokeWithMatchers(
            Sym var,
            Sym invokeVar,
            yokohama.unit.ast.ClassType classToStub,
            Type returnType,
            MethodPattern methodPattern,
            Span span) {
        /*
        a. Prepare matchers (`argMatchers`)
        b. Invoke the method with the matchers (`invoke`)
        */
        String methodName = methodPattern.getName();
        boolean isVararg = methodPattern.isVararg();
        List<yokohama.unit.ast.Type> argumentTypes = methodPattern.getParamTypes();
        Stream<Type> argTypes;
        Stream<Sym> argVars;
        Stream<Statement> argMatchers;
        if (isVararg) {
            Sym varArg = genSym.generate("varArg");
            List<Pair<Sym, Stream<Statement>>> pairs = argumentTypes.subList(0, argumentTypes.size() - 1).stream()
                    .map(argumentType -> mapArgumentType(argumentType, classResolver))
                    .collect(Collectors.toList());
            argTypes = Stream.concat(
                    argumentTypes.subList(0, argumentTypes.size() - 1)
                            .stream()
                            .map(type -> Type.of(type, classResolver)),
                    Stream.of(
                            Type.of(argumentTypes.get(argumentTypes.size() - 1), classResolver).toArray()));
            argVars = Stream.concat(pairs.stream().map(Pair::getFirst),
                    Stream.of(varArg));
            argMatchers = Stream.concat(
                    pairs.stream().flatMap(Pair::getSecond),
                    Stream.<Statement>of(
                            new VarInitStatement(
                                    Type.of(argumentTypes.get(argumentTypes.size() - 1), classResolver).toArray(),
                                    varArg,
                                    new InvokeStaticExpr(
                                            classTypeOf(MOCKITO),
                                            Arrays.asList(
                                                    Type.of(argumentTypes.get(argumentTypes.size() - 1), classResolver).toArray()),
                                            "anyVararg",
                                            Arrays.asList(),
                                            Arrays.asList(),
                                            Type.OBJECT),
                                    span)));
        } else {
            List<Pair<Sym, Stream<Statement>>> pairs = argumentTypes.stream()
                    .map(argumentType -> mapArgumentType(argumentType, classResolver))
                    .collect(Collectors.toList());
            argTypes = methodPattern.getParamTypes().stream().map(type -> Type.of(type, classResolver));
            argVars = pairs.stream().map(Pair::getFirst);
            argMatchers = pairs.stream().flatMap(Pair::getSecond);
        }

        // invoke the method
        Sym invokeTmpVar = returnType.isPrimitive() ? genSym.generate("invoke") : invokeVar;
        Stream<Statement> invoke = Stream.concat(
                Stream.of(new VarInitStatement(
                        returnType,
                        invokeTmpVar, 
                        new InvokeExpr(
                                ClassType.of(classToStub, classResolver),
                                var,
                                methodName,
                                argTypes.collect(Collectors.toList()),
                                argVars.collect(Collectors.toList()),
                                returnType),
                        span)),
                // box primitive type if needed
                returnType.getDims() == 0
                        ? returnType.getNonArrayType().accept(primitiveType -> {
                                    ClassType boxed = primitiveType.box();
                                    return Stream.of(new VarInitStatement(boxed.toType(), invokeVar,
                                                    new InvokeStaticExpr(
                                                            boxed,
                                                            Arrays.asList(),
                                                            "valueOf",
                                                            Arrays.asList(primitiveType.toType()),
                                                            Arrays.asList(invokeTmpVar),
                                                            boxed.toType()),
                                                    span));
                                },
                                classType -> Stream.empty())
                        : Stream.empty());
        return Stream.concat(argMatchers, invoke);
    }

    private Stream<Statement> whenThenReturn(
            Sym invokeVar, Sym returnedVar, Span span) {
        Sym stubbingVar = genSym.generate("stubbing");
        Sym __ = genSym.generate("__");
        Stream<Statement> whenThenReturn = Stream.of(
                new VarInitStatement(
                        typeOf(ONGOING_STUBBING),
                        stubbingVar,
                        new InvokeStaticExpr(
                                classTypeOf(MOCKITO),
                                Arrays.asList(),
                                "when",
                                Arrays.asList(Type.OBJECT),
                                Arrays.asList(invokeVar),
                                typeOf(ONGOING_STUBBING)),
                        span),
                new VarInitStatement(Type.OBJECT, __, 
                        new InvokeExpr(
                                classTypeOf(ONGOING_STUBBING),
                                stubbingVar,
                                "thenReturn",
                                Arrays.asList(Type.OBJECT),
                                Arrays.asList(returnedVar),
                                typeOf(ONGOING_STUBBING)),
                        span));
        return whenThenReturn;
    }

    private Pair<Sym, Stream<Statement>> mapArgumentType(
            yokohama.unit.ast.Type argumentType,
            ClassResolver classResolver) {
        Span span = argumentType.getSpan();
        Sym argVar = genSym.generate("arg");
        int dims = argumentType.getDims();
        Stream<Statement> statements = argumentType.getNonArrayType().accept(primitiveType -> {
                    if (dims == 0) {
                        switch (primitiveType.getKind()) {
                            case BOOLEAN:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.BOOLEAN, argVar,
                                                new InvokeStaticExpr(
                                                        classTypeOf(MOCKITO),
                                                        Arrays.asList(),
                                                        "anyBoolean",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.BOOLEAN),
                                                span));
                            case BYTE:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.BYTE, argVar,
                                                new InvokeStaticExpr(
                                                        classTypeOf(MOCKITO),
                                                        Arrays.asList(),
                                                        "anyByte",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.BYTE),
                                                span));
                            case SHORT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.SHORT, argVar,
                                                new InvokeStaticExpr(
                                                        classTypeOf(MOCKITO),
                                                        Arrays.asList(),
                                                        "anyShort",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.SHORT),
                                                span));
                            case INT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.INT, argVar,
                                                new InvokeStaticExpr(
                                                        classTypeOf(MOCKITO),
                                                        Arrays.asList(),
                                                        "anyInt",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.INT),
                                                span));
                            case LONG:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.LONG, argVar,
                                                new InvokeStaticExpr(
                                                        classTypeOf(MOCKITO),
                                                        Arrays.asList(),
                                                        "anyLong",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.LONG),
                                                span));
                            case CHAR:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.CHAR, argVar,
                                                new InvokeStaticExpr(
                                                        classTypeOf(MOCKITO),
                                                        Arrays.asList(),
                                                        "anyChar",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.CHAR),
                                                span));
                            case FLOAT:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.FLOAT, argVar,
                                                new InvokeStaticExpr(
                                                        classTypeOf(MOCKITO),
                                                        Arrays.asList(),
                                                        "anyFloat",
                                                        Arrays.asList(),
                                                        Arrays.asList(),
                                                        Type.FLOAT),
                                                span));
                            case DOUBLE:
                                return Stream.<Statement>of(
                                        new VarInitStatement(Type.DOUBLE, argVar,
                                                new InvokeStaticExpr(
                                                        classTypeOf(MOCKITO),
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
                        Sym clazzVar = genSym.generate("clazz");
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
                        return Stream.<Statement>of(new VarInitStatement(Type.CLASS, clazzVar, new ClassLitExpr(type), span),
                                new VarInitStatement(type, argVar,
                                        new InvokeStaticExpr(
                                                classTypeOf(MOCKITO),
                                                Arrays.asList(type),
                                                "isA",
                                                Arrays.asList(Type.CLASS),
                                                Arrays.asList(clazzVar),
                                                Type.OBJECT),
                                        Span.dummySpan()));
                    }
                },
                classType -> {
                    Sym clazzVar = genSym.generate("clazz");
                    Type type = new Type(
                            ClassType.of(classType, classResolver),
                            dims);
                    return Stream.<Statement>of(new VarInitStatement(Type.CLASS, clazzVar, new ClassLitExpr(type), span),
                            new VarInitStatement(type, argVar,
                                    new InvokeStaticExpr(
                                            classTypeOf(MOCKITO),
                                            Arrays.asList(type),
                                            "isA",
                                            Arrays.asList(Type.CLASS),
                                            Arrays.asList(clazzVar),
                                            Type.OBJECT),
                                    Span.dummySpan()));
                });
        return new Pair<>(argVar, statements);
    }
}
