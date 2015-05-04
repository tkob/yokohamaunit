package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.BooleanExpr;
import yokohama.unit.ast.CharExpr;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.FloatingPointExpr;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.InstanceOfMatcher;
import yokohama.unit.ast.InstanceSuchThatMatcher;
import yokohama.unit.ast.InvocationExpr;
import yokohama.unit.ast.LetBindings;
import yokohama.unit.ast.Matcher;
import yokohama.unit.ast.MethodPattern;
import yokohama.unit.ast.NullValueMatcher;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.Predicate;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.StringExpr;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableExtractVisitor;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.Test;
import yokohama.unit.ast_junit.Annotation;
import yokohama.unit.ast_junit.BooleanLitExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.CharLitExpr;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.DoubleLitExpr;
import yokohama.unit.ast_junit.EqualToMatcherExpr;
import yokohama.unit.ast_junit.FloatLitExpr;
import yokohama.unit.ast_junit.InstanceOfMatcherExpr;
import yokohama.unit.ast_junit.IntLitExpr;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeExpr.Instruction;
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.LongLitExpr;
import yokohama.unit.ast_junit.Method;
import yokohama.unit.ast_junit.NonArrayType;
import yokohama.unit.ast_junit.NullExpr;
import yokohama.unit.ast_junit.NullValueMatcherExpr;
import yokohama.unit.ast_junit.PrimitiveType;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.TryStatement;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.position.Position;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Lists;
import yokohama.unit.util.Optionals;
import yokohama.unit.util.Pair;
import yokohama.unit.util.SUtils;

@AllArgsConstructor
public class AstToJUnitAst {
    private final String name;
    private final String packageName;
    ExpressionStrategy expressionStrategy;
    MockStrategy mockStrategy;
    GenSym genSym;
    ClassResolver classResolver;
    TableExtractVisitor tableExtractVisitor;

    public CompilationUnit translate(Group group) {
        List<Definition> definitions = group.getDefinitions();
        final List<Table> tables = tableExtractVisitor.extractTables(group);
        List<Method> methods =
                definitions.stream()
                           .flatMap(definition -> definition.accept(
                                   test -> translateTest(test, tables).stream(),
                                   fourPhaseTest -> translateFourPhaseTest(fourPhaseTest, tables).stream(),
                                   table -> Stream.empty()))
                           .collect(Collectors.toList());
        ClassDecl testClass = new ClassDecl(true, name, Optional.empty(), Arrays.asList(), methods);
        Stream<ClassDecl> auxClasses = Stream.concat(
                expressionStrategy.auxClasses(classResolver).stream(),
                mockStrategy.auxClasses(classResolver).stream());
        List<ClassDecl> classes =
                Stream.concat(auxClasses, Stream.of(testClass))
                        .collect(Collectors.toList());
        return new CompilationUnit(packageName, classes);
    }

    List<Method> translateTest(Test test, final List<Table> tables) {
        final String name = test.getName();
        List<Assertion> assertions = test.getAssertions();
        List<Method> methods = 
                IntStream.range(0, assertions.size())
                        .mapToObj(Integer::new)
                        .flatMap(i -> translateAssertion(assertions.get(i), i + 1, name, tables).stream())
                        .collect(Collectors.toList());
        return methods;
    }

    List<Method> translateAssertion(Assertion assertion, int index, String testName, List<Table> tables) {
        String methodName = SUtils.toIdent(testName) + "_" + index;
        List<Proposition> propositions = assertion.getPropositions();
        return assertion.getFixture().accept(() -> {
                    String env = genSym.generate("env");
                    return Arrays.asList(new Method(
                                    Arrays.asList(Annotation.TEST),
                                    methodName,
                                    Arrays.asList(),
                                    Optional.empty(),
                                    Arrays.asList(new ClassType(java.lang.Exception.class, Span.dummySpan())),
                                    ListUtils.union(
                                            expressionStrategy.env(env, classResolver),
                                            propositions.stream()
                                                    .flatMap(proposition ->
                                                                translateProposition(
                                                                        proposition,
                                                                        env))
                                                    .collect(Collectors.toList()))));
                },
                tableRef -> {
                    String env = genSym.generate("env");
                    List<List<Statement>> table = translateTableRef(tableRef, tables, env);
                    return IntStream.range(0, table.size())
                            .mapToObj(Integer::new)
                            .map(i -> {
                                return new Method(
                                        Arrays.asList(Annotation.TEST),
                                        methodName + "_" + (i + 1),
                                        Arrays.asList(),
                                        Optional.empty(),
                                        Arrays.asList(new ClassType(java.lang.Exception.class, Span.dummySpan())),
                                        ListUtils.union(
                                                expressionStrategy.env(env, classResolver),
                                                ListUtils.union(
                                                        table.get(i),
                                                        propositions
                                                                .stream()
                                                                .flatMap(proposition ->
                                                                        translateProposition(
                                                                                proposition,
                                                                                env))
                                                                .collect(Collectors.toList()))));
                            })
                            .collect(Collectors.toList());
                },
                bindings -> {
                    String env = genSym.generate("env");
                    return Arrays.asList(new Method(
                            Arrays.asList(Annotation.TEST),
                            methodName,
                            Arrays.asList(),
                            Optional.empty(),
                            Arrays.asList(new ClassType(java.lang.Exception.class, Span.dummySpan())),
                            ListUtils.union(
                                    expressionStrategy.env(env, classResolver),
                                    Stream.concat(
                                            bindings.getBindings()
                                                    .stream()
                                                    .flatMap(binding ->
                                                            translateBinding(
                                                                    binding,
                                                                    env)),
                                            propositions.stream()
                                                    .flatMap(proposition ->
                                                            translateProposition(
                                                                    proposition,
                                                                    env)))
                                            .collect(Collectors.toList()))));
                });
    }

    Stream<Statement> translateProposition(Proposition proposition, String envVarName) {
        String actual = genSym.generate("actual");
        String expected = genSym.generate("expected");
        Predicate predicate = proposition.getPredicate();
        Stream<Statement> subjectAndPredicate = predicate.<Stream<Statement>>accept(
                isPredicate -> {
                    return Stream.concat(
                            expressionStrategy.eval(
                                    actual, proposition.getSubject(), Object.class, envVarName).stream(),
                            translateMatcher(
                                    isPredicate.getComplement(),
                                    expected,
                                    actual,
                                    envVarName));
                },
                isNotPredicate -> {
                    // inhibit `is not instance e of Exception such that...`
                    isNotPredicate.getComplement().accept(
                            equalTo -> null,
                            instanceOf -> null,
                            instanceSuchThat -> {
                                throw new TranslationException(
                                        "`instance _ of _ such that` cannot follow `is not`",
                                        instanceSuchThat.getSpan());
                            },
                            nullValue -> null);
                    String unexpected = genSym.generate("unexpected");
                    return Stream.concat(
                            expressionStrategy.eval(
                                    actual, proposition.getSubject(), Object.class, envVarName).stream(),
                            Stream.concat(
                                    translateMatcher(isNotPredicate.getComplement(),
                                            unexpected,
                                            actual,
                                            envVarName),
                                    Stream.of(new VarInitStatement(
                                            Type.MATCHER,
                                            expected,
                                            new InvokeStaticExpr(
                                                    new ClassType(org.hamcrest.CoreMatchers.class, Span.dummySpan()),
                                                    Arrays.asList(),
                                                    "not",
                                                    Arrays.asList(Type.MATCHER),
                                                    Arrays.asList(new Var(unexpected)),
                                                    Type.MATCHER),
                                            predicate.getSpan()))));
                },
                throwsPredicate -> {
                    String __ = genSym.generate("tmp");
                    return Stream.concat(
                            bindThrown(
                                    actual,
                                    expressionStrategy.eval(
                                            __, proposition.getSubject(), Object.class, envVarName),
                                    envVarName),
                            translateMatcher(
                                    throwsPredicate.getThrowee(),
                                    expected,
                                    actual,
                                    envVarName));
                }
        );
        Matcher matcher = predicate.accept(
                isPredicate -> isPredicate.getComplement(),
                isNotPredicate -> isNotPredicate.getComplement(),
                throwsPredicate -> throwsPredicate.getThrowee());
        return Stream.concat(
                subjectAndPredicate,
                matcher instanceof InstanceSuchThatMatcher
                        ? Stream.empty()
                        : Stream.of(new IsStatement(new Var(actual), new Var(expected), predicate.getSpan())));
    }

    Stream<Statement> bindThrown(String actual, List<Statement> statements, String envVarName) {
        String e = genSym.generate("ex");
        /*
        Throwable actual;
        try {
            // statements
            ...
            actual = null;
        } catch (XXXXException e) { // extract the cause if wrapped: inserted by the strategy
            actual = e.get...;
        } catch (Throwable e) {
            actual = e;
        }
        */
        return Stream.of(
                new TryStatement(
                        ListUtils.union(
                                statements,
                                Arrays.asList(new VarInitStatement(
                                        Type.THROWABLE, actual, new NullExpr(), Span.dummySpan()))),
                        Stream.concat(
                                Optionals.toStream(expressionStrategy.catchAndAssignCause(actual)),
                                Stream.of(new CatchClause(
                                        new ClassType(java.lang.Throwable.class, Span.dummySpan()),
                                        new Var(e),
                                        Arrays.asList(new VarInitStatement(
                                                Type.THROWABLE, actual, new VarExpr(e), Span.dummySpan())))))
                                .collect(Collectors.toList()),
                        Arrays.asList()));
    }

    private String lookupClassName(String name, Span span) {
        try {
            return classResolver.lookup(name).getCanonicalName();
        } catch (ClassNotFoundException e) {
            throw new TranslationException(e.getMessage(), span, e);
        }
    }

    Stream<Statement> translateMatcher(
            Matcher matcher,
            String varName,
            String actual,
            String envVarName) {
        return matcher.<Stream<Statement>>accept(
            (EqualToMatcher equalTo) -> {
                Var objVar = new Var(genSym.generate("obj"));
                return Stream.concat(
                        expressionStrategy.eval(
                                objVar.getName(), equalTo.getExpr(), Object.class, envVarName).stream(),
                        Stream.of(new VarInitStatement(
                                Type.MATCHER,
                                varName,
                                new EqualToMatcherExpr(objVar),
                                Span.dummySpan())));
            },
            (InstanceOfMatcher instanceOf) -> {
                return Stream.of(new VarInitStatement(
                        Type.MATCHER,
                        varName,
                        new InstanceOfMatcherExpr(
                                lookupClassName(
                                        instanceOf.getClazz().getName(),
                                        instanceOf.getSpan())),
                        instanceOf.getSpan()));
            },
            (InstanceSuchThatMatcher instanceSuchThat) -> {
                String bindVarName = instanceSuchThat.getVar().getName();
                yokohama.unit.ast.ClassType clazz = instanceSuchThat.getClazz();
                List<Proposition> propositions = instanceSuchThat.getPropositions();
                Span span = instanceSuchThat.getSpan();

                String instanceOfVarName = genSym.generate("instanceOfMatcher");
                Stream<Statement> instanceOfStatements = Stream.of(
                        new VarInitStatement(
                                Type.MATCHER,
                                instanceOfVarName,
                                new InstanceOfMatcherExpr(
                                        lookupClassName(
                                                clazz.getName(),
                                                clazz.getSpan())),
                                clazz.getSpan()),
                        new IsStatement(
                                new Var(actual),
                                new Var(instanceOfVarName),
                                clazz.getSpan()));

                Stream<Statement> bindStatements =
                        expressionStrategy.bind(envVarName, bindVarName, new Var(actual)).stream();

                Stream<Statement> suchThatStatements =
                        propositions
                                .stream()
                                .flatMap(proposition ->
                                        translateProposition(proposition, envVarName));

                return Stream.concat(
                        instanceOfStatements,
                        Stream.concat(
                                bindStatements,
                                suchThatStatements));
            },
            (NullValueMatcher nullValue) -> {
                return Stream.of(
                        new VarInitStatement(
                                Type.MATCHER,
                                varName,
                                new NullValueMatcherExpr(),
                                nullValue.getSpan()));
            });
    }

    Stream<Statement> translateBinding(yokohama.unit.ast.Binding binding, String envVarName) {
        String name = binding.getName().getName();
        String varName = genSym.generate(name);
        return Stream.concat(
                translateExpr(binding.getValue(), varName, Object.class, envVarName),
                expressionStrategy.bind(envVarName, name, new Var(varName)).stream());
    }

    Stream<Statement> translateExpr(
            yokohama.unit.ast.Expr expr,
            String varName,
            Class<?> expectedType,
            String envVarName) {
        Var exprVar = new Var(genSym.generate("expr"));
        Pair<Type, Stream<Statement>> returnTypeAndStatements = expr.accept(
                quotedExpr -> {
                    Type returnType = Type.OBJECT;
                    Stream<Statement> statements = expressionStrategy.eval(
                            exprVar.getName(), quotedExpr, Type.fromClass(expectedType).box().toClass(), envVarName).stream();
                    return new Pair<>(returnType, statements);
                },
                stubExpr -> {
                    Type returnType = Type.of(
                            new yokohama.unit.ast.Type(stubExpr.getClassToStub(), 0, Span.dummySpan()),
                            classResolver);
                    Stream<Statement> statements = mockStrategy.stub(
                            exprVar.getName(), stubExpr, this, envVarName, classResolver).stream();
                    return new Pair<>(returnType, statements);
                },
                invocationExpr -> {
                    return translateInvocationExpr(
                            invocationExpr, exprVar.getName(), envVarName);
                },
                integerExpr -> {
                    Type returnType = integerExpr.match(
                            intValue -> Type.INT,
                            longValue -> Type.LONG);
                    Stream<Statement> statements = integerExpr.match(
                            intValue -> {
                                return Stream.<Statement>of(
                                        new VarInitStatement(
                                                Type.INT,
                                                exprVar.getName(),
                                                new IntLitExpr(intValue),
                                                integerExpr.getSpan()));
                            },
                            longValue -> {
                                return Stream.<Statement>of(
                                        new VarInitStatement(
                                                Type.LONG,
                                                exprVar.getName(),
                                                new LongLitExpr(longValue),
                                                integerExpr.getSpan()));
                            });
                    return new Pair<>(returnType, statements);
                },
                floatingPointExpr -> {
                    Type returnType = floatingPointExpr.match(
                            floatValue -> Type.FLOAT,
                            doubleValue -> Type.DOUBLE);
                    Stream<Statement> statements =
                            translateFloatingPointExpr(floatingPointExpr, exprVar.getName(), envVarName);
                    return new Pair<>(returnType, statements);
                },
                booleanExpr -> {
                    Type returnType = Type.BOOLEAN;
                    Stream<Statement> statements =
                            translateBooleanExpr(booleanExpr, exprVar.getName(), envVarName);
                    return new Pair<>(returnType, statements);
                },
                charExpr -> {
                    Type returnType = Type.CHAR;
                    Stream<Statement> statements =
                            translateCharExpr(charExpr, exprVar.getName(), envVarName);
                    return new Pair<>(returnType, statements);
                },
                stringExpr -> {
                    Type returnType = Type.STRING;
                    Stream<Statement> statements =
                            translateStringExpr(stringExpr, exprVar.getName(), envVarName);
                    return new Pair<>(returnType, statements);
                });

        // box or unbox if needed
        Stream<Statement> boxing =
                boxOrUnbox(
                        Type.fromClass(expectedType),
                        varName,
                        returnTypeAndStatements.getFirst(),
                        exprVar);

        return Stream.concat(returnTypeAndStatements.getSecond(), boxing);
    }

    Pair<Type, Stream<Statement>> translateInvocationExpr(
            InvocationExpr invocationExpr,
            String varName,
            String envVarName) {
        yokohama.unit.ast.ClassType classType = invocationExpr.getClassType();
        Class<?> clazz = classType.toClass(classResolver);
        MethodPattern methodPattern = invocationExpr.getMethodPattern();
        String methodName = methodPattern.getName();
        List<yokohama.unit.ast.Type> argTypes = methodPattern.getArgumentTypes();
        boolean isVarArg = methodPattern.isVarArg();
        Optional<Ident> receiver = invocationExpr.getReceiver();
        List<yokohama.unit.ast.Expr> args = invocationExpr.getArgs();

        Type returnType = getReturnType(classType, methodName, argTypes, isVarArg);

        List<Pair<Var, Stream<Statement>>> setupArgs;
        if (isVarArg) {
            // extend argument types if args are variable length
            List<yokohama.unit.ast.Type> extendedArgTypes = isVarArg
                    ? ListUtils.union(argTypes,
                            Lists.repeat(Lists.last(argTypes),
                                    args.size() - argTypes.size()))
                    : argTypes;
            List<Pair<yokohama.unit.ast.Type, yokohama.unit.ast.Expr>> pairs =
                    Pair.<yokohama.unit.ast.Type, yokohama.unit.ast.Expr>zip(
                            extendedArgTypes, args);
            throw new UnsupportedOperationException();
        } else {
            List<Pair<yokohama.unit.ast.Type, yokohama.unit.ast.Expr>> pairs =
                    Pair.<yokohama.unit.ast.Type, yokohama.unit.ast.Expr>zip(
                            argTypes, args);
            setupArgs = pairs.stream().map(pair -> pair.map((t, arg) -> {
                        // evaluate actual args and coerce to parameter types
                        Var argVar = new Var(genSym.generate("arg"));
                        Type paramType = Type.of(t, classResolver);
                        Stream<Statement> expr = translateExpr(
                                arg, argVar.getName(), paramType.toClass(), envVarName);
                        return new Pair<>(argVar, expr);
                    })).collect(Collectors.toList());
        }
        List<Var> argVars = Pair.unzip(setupArgs).getFirst();
        Stream<Statement> setupStatements =
                setupArgs.stream().flatMap(Pair::getSecond);

        Stream<Statement> invocation;
        if (receiver.isPresent()) {
            // invokevirtual
            Type receiverType = Type.fromClass(clazz);
            Var receiverVar = new Var(genSym.generate("receiver"));
            Stream<Statement> getReceiver =
                    expressionStrategy.eval(
                            receiverVar.getName(),
                            new QuotedExpr(receiver.get().getName(), Span.dummySpan()),
                            receiverType.toClass(),
                            envVarName)
                            .stream();
            invocation = Stream.concat(getReceiver, Stream.of(
                    new VarInitStatement(
                            returnType,
                            varName,
                            new InvokeExpr(
                                    clazz.isInterface()
                                            ? Instruction.INTERFACE
                                            : Instruction.VIRTUAL,
                                    receiverVar,
                                    methodName,
                                    argTypes.stream().map(type -> Type.of(type, classResolver)).collect(Collectors.toList()),
                                    argVars,
                                    returnType),
                            Span.dummySpan())));
        } else {
            // invokestatic
            invocation = Stream.of(
                    new VarInitStatement(
                            returnType,
                            varName,
                            new InvokeStaticExpr(
                                    ClassType.of(classType, classResolver),
                                    Collections.emptyList(),
                                    methodName,
                                    argTypes.stream().map(type -> Type.of(type, classResolver)).collect(Collectors.toList()),
                                    argVars,
                                    returnType),
                            Span.dummySpan()));
        }

        return new Pair<>(returnType, Stream.concat(setupStatements,invocation));
    }

    @SneakyThrows(NoSuchMethodException.class)
    public Type getReturnType(
            yokohama.unit.ast.ClassType classType,
            String methodName,
            List<yokohama.unit.ast.Type> argumentTypes,
            boolean isVarArg) {
        Class<?> clazz = classType.toClass(classResolver);
        java.lang.reflect.Method method = clazz.getMethod(
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

    Stream<Statement> translateFloatingPointExpr(
            FloatingPointExpr floatingPointExpr,
            String varName,
            String envVarName) {
        return floatingPointExpr.match(
                floatValue -> {
                    return Stream.<Statement>of(
                            new VarInitStatement(
                                    Type.FLOAT,
                                    varName,
                                    new FloatLitExpr(floatValue),
                                    floatingPointExpr.getSpan()));
                },
                doubleValue -> {
                    return Stream.<Statement>of(
                            new VarInitStatement(
                                    Type.DOUBLE,
                                    varName,
                                    new DoubleLitExpr(doubleValue),
                                    floatingPointExpr.getSpan()));
                });
    }

    Stream<Statement> translateBooleanExpr(
            BooleanExpr booleanExpr, String varName, String envVarName) {
        boolean booleanValue = booleanExpr.getValue();
        return Stream.<Statement>of(
                new VarInitStatement(
                        Type.BOOLEAN,
                        varName,
                        new BooleanLitExpr(booleanValue),
                        booleanExpr.getSpan()));
    }

    Stream<Statement> translateCharExpr(
            CharExpr charExpr, String varName, String envVarName) {
        char charValue = charExpr.getValue();
        return Stream.<Statement>of(
                new VarInitStatement(
                        Type.CHAR,
                        varName,
                        new CharLitExpr(charValue),
                        charExpr.getSpan()));
    }

    Stream<Statement> translateStringExpr(
            StringExpr stringExpr, String varName, String envVarName) {
        String strValue = stringExpr.getValue();
        return Stream.<Statement>of(
                new VarInitStatement(
                        Type.STRING,
                        varName,
                        new StrLitExpr(strValue),
                        stringExpr.getSpan()));
    }

    public Stream<Statement> boxOrUnbox(
            Type toType, String toVarName, Type fromType, Var fromVar) {
        return fromType.<Stream<Statement>>matchPrimitiveOrNot(
                primitiveType -> {
                    return fromPrimitive(
                            toType, toVarName, primitiveType, fromVar);
                },
                nonPrimitiveType -> {
                    return fromNonPrimtive(toType, toVarName, fromVar);
                });
    }
    
    private Stream<Statement> fromPrimitive(
            Type toType, String toVarName, PrimitiveType fromType, Var fromVar) {
        return toType.<Stream<Statement>>matchPrimitiveOrNot(
                primitiveType -> {
                    return Stream.of(
                            new VarInitStatement(
                                    toType,
                                    toVarName,
                                    new VarExpr(fromVar.getName()),
                                    Span.dummySpan()));
                },
                nonPrimitiveType -> {
                    return Stream.of(new VarInitStatement(
                            nonPrimitiveType,
                            toVarName,
                            new InvokeStaticExpr(
                                    fromType.box(),
                                    Arrays.asList(),
                                    "valueOf",
                                    Arrays.asList(fromType.toType()),
                                    Arrays.asList(fromVar),
                                    fromType.box().toType()),
                            Span.dummySpan()));

                });
    }

    private Stream<Statement> fromNonPrimtive(
            Type toType, String toVarName, Var fromVar) {
        // precondition: fromVar is not primitive
        return toType.<Stream<Statement>>matchPrimitiveOrNot(
                primitiveType -> {
                    Var boxVar = new Var(genSym.generate("box"));
                    Type boxType;
                    String unboxMethodName;
                    switch (primitiveType.getKind()) {
                        case BOOLEAN:
                            boxType = primitiveType.box().toType();
                            unboxMethodName = "booleanValue";
                            break;
                        case BYTE:
                            boxType = Type.fromClass(Number.class);
                            unboxMethodName = "byteValue";
                            break;
                        case SHORT:
                            boxType = Type.fromClass(Number.class);
                            unboxMethodName = "shortValue";
                            break;
                        case INT:
                            boxType = Type.fromClass(Number.class);
                            unboxMethodName = "intValue";
                            break;
                        case LONG:
                            boxType = Type.fromClass(Number.class);
                            unboxMethodName = "longValue";
                            break;
                        case CHAR:
                            boxType = primitiveType.box().toType();
                            unboxMethodName = "charValue";
                            break;
                        case FLOAT:
                            boxType = Type.fromClass(Number.class);
                            unboxMethodName = "floatValue";
                            break;
                        case DOUBLE:
                            boxType = Type.fromClass(Number.class);
                            unboxMethodName = "doubleValue";
                            break;
                        default:
                            throw new RuntimeException("should not reach here");
                    }
                    return Stream.of(
                            new VarInitStatement(
                                    boxType,
                                    boxVar.getName(),
                                    new VarExpr(fromVar.getName()),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    toType,
                                    toVarName,
                                    new InvokeExpr(
                                            Instruction.VIRTUAL,
                                            fromVar,
                                            unboxMethodName,
                                            Collections.emptyList(),
                                            Collections.emptyList(),
                                            toType),
                                    Span.dummySpan()));
                },
                nonPrimitiveType -> {
                    return Stream.of(
                            new VarInitStatement(
                                    nonPrimitiveType,
                                    toVarName,
                                    new VarExpr(fromVar.getName()),
                                    Span.dummySpan()));
                });
    }

    Type translateType(yokohama.unit.ast.Type type) {
        return new Type(
                translateNonArrayType(type.getNonArrayType()), type.getDims());
    }

    NonArrayType translateNonArrayType(yokohama.unit.ast.NonArrayType nonArrayType) {
        return nonArrayType.accept(
                primitiveType -> new PrimitiveType(primitiveType.getKind()),
                classType -> new ClassType(
                        classType.toClass(classResolver),
                        classType.getSpan()));
    }

    List<List<Statement>> translateTableRef(
            TableRef tableRef,
            List<Table> tables,
            String envVarName) {
        String name = tableRef.getName();
        List<String> idents = tableRef.getIdents()
                                      .stream()
                                      .map(Ident::getName)
                                      .collect(Collectors.toList());
        try {
            switch(tableRef.getType()) {
                case INLINE:
                    return translateTable(
                            tables.stream()
                                  .filter(table -> table.getName().equals(name))
                                  .findFirst()
                                  .get(),
                            idents,
                            envVarName);
                case CSV:
                    return parseCSV(name, CSVFormat.DEFAULT.withHeader(), idents, envVarName);
                case TSV:
                    return parseCSV(name, CSVFormat.TDF.withHeader(), idents, envVarName);
                case EXCEL:
                    return parseExcel(name, idents, envVarName);
            }
            throw new IllegalArgumentException("'" + Objects.toString(tableRef) + "' is not a table reference.");
        } catch (InvalidFormatException | IOException e) {
            throw new TranslationException(e.getMessage(), tableRef.getSpan(), e);
        }

    }

    List<List<Statement>> translateTable(
            Table table,
            List<String> idents,
            String envVarName) {
        return table.getRows()
                    .stream()
                    .map(row ->
                            translateRow(
                                    row,
                                    table.getHeader()
                                            .stream()
                                            .map(Ident::getName)
                                            .collect(Collectors.toList()),
                                    idents,
                                    envVarName))
                    .collect(Collectors.toList());
    }

    List<Statement> translateRow(
            Row row,
            List<String> header,
            List<String> idents,
            String envVarName) {
        return IntStream.range(0, header.size())
                .filter(i -> idents.contains(header.get(i)))
                .mapToObj(Integer::new)
                .flatMap(i -> {
                    String varName = genSym.generate(header.get(i));
                    return Stream.concat(
                            translateExpr(row.getExprs().get(i), varName, Object.class, envVarName),
                            expressionStrategy.bind(envVarName, header.get(i), new Var(varName)).stream());
                })
                .collect(Collectors.toList());
    }

    List<List<Statement>> parseCSV(
            String fileName, CSVFormat format, List<String> idents, String envVarName)
            throws IOException {
        try (   final InputStream in = getClass().getResourceAsStream(fileName);
                final Reader reader = new InputStreamReader(in, "UTF-8");
                final CSVParser parser = new CSVParser(reader, format)) {
            return StreamSupport.stream(parser.spliterator(), false)
                    .map(record ->
                            parser.getHeaderMap().keySet()
                                    .stream()
                                    .filter(key -> idents.contains(key))
                                    .flatMap(name -> {
                                        String varName = genSym.generate(name);
                                        return Stream.concat(expressionStrategy.eval(
                                                varName,
                                                new yokohama.unit.ast.QuotedExpr(
                                                        record.get(name),
                                                        new yokohama.unit.position.Span(
                                                                Optional.of(Paths.get(fileName)),
                                                                new Position((int)parser.getCurrentLineNumber(), -1),
                                                                new Position(-1, -1))),
                                                Object.class, envVarName).stream(),
                                                expressionStrategy.bind(envVarName, name, new Var(varName)).stream());
                                    })
                                    .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<List<Statement>> parseExcel(
            String fileName, List<String> idents, String envVarName)
            throws InvalidFormatException, IOException {
        try (InputStream in = getClass().getResourceAsStream(fileName)) {
            final Workbook book = WorkbookFactory.create(in);
            final Sheet sheet = book.getSheetAt(0);
            final int top = sheet.getFirstRowNum();
            final int left = sheet.getRow(top).getFirstCellNum();
            List<String> names = StreamSupport.stream(sheet.getRow(top).spliterator(), false)
                    .map(cell -> cell.getStringCellValue())
                    .collect(Collectors.toList());
            return StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(row -> 
                        IntStream.range(0, names.size())
                                .filter(i -> idents.contains(names.get(i)))
                                .mapToObj(Integer::new)
                                .flatMap(i -> {
                                    String varName = genSym.generate(names.get(i));
                                    return Stream.concat(expressionStrategy.eval(
                                            varName,
                                            new yokohama.unit.ast.QuotedExpr(
                                                    row.getCell(left + i).getStringCellValue(),
                                                    new yokohama.unit.position.Span(
                                                            Optional.of(Paths.get(fileName)),
                                                            new Position(row.getRowNum() + 1, left + i + 1),
                                                            new Position(-1, -1))),
                                            Object.class, envVarName).stream(),
                                            expressionStrategy.bind(envVarName, names.get(i), new Var(varName)).stream());
                                })
                                .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<Method> translateFourPhaseTest(FourPhaseTest fourPhaseTest, List<Table> tables) {
        String env = genSym.generate("env");

        String testName = SUtils.toIdent(fourPhaseTest.getName());
        Stream<Statement> bindings;
        if (fourPhaseTest.getSetup().isPresent()) {
            Phase setup = fourPhaseTest.getSetup().get();
            if (setup.getLetBindings().isPresent()) {
                LetBindings letBindings = setup.getLetBindings().get();
                bindings = letBindings.getBindings()
                        .stream()
                        .flatMap(binding -> {
                            String varName = genSym.generate(binding.getName());
                            return Stream.concat(
                                    translateExpr(binding.getValue(), varName, Object.class, env),
                                    expressionStrategy.bind(env, binding.getName(), new Var(varName)).stream());
                        });
            } else {
                bindings = Stream.empty();
            }
        } else {
            bindings = Stream.empty();
        }

        Optional<Stream<Statement>> setupActions =
                fourPhaseTest.getSetup()
                        .map(Phase::getExecutions)
                        .map(execition -> translateExecutions(execition, env));
        Optional<Stream<Statement>> exerciseActions =
                fourPhaseTest.getExercise()
                        .map(Phase::getExecutions)
                        .map(execution -> translateExecutions(execution, env));
        Stream<Statement> testStatements = fourPhaseTest.getVerify().getAssertions()
                .stream()
                .flatMap(assertion ->
                        assertion.getPropositions()
                                .stream()
                                .flatMap(proposition ->
                                        translateProposition(proposition, env)));

        List<Statement> statements =
                Stream.concat(
                        bindings,
                        Stream.concat(
                                Stream.concat(
                                        setupActions.isPresent() ? setupActions.get() : Stream.empty(),
                                        exerciseActions.isPresent() ? exerciseActions.get() : Stream.empty()),
                                testStatements)
                ).collect(Collectors.toList());


        List<Statement> actionsAfter;
        if (fourPhaseTest.getTeardown().isPresent()) {
            Phase teardown = fourPhaseTest.getTeardown().get();
            actionsAfter = translateExecutions(teardown.getExecutions(), env).collect(Collectors.toList());
        } else {
            actionsAfter = Arrays.asList();
        }

        return Arrays.asList(new Method(
                Arrays.asList(Annotation.TEST),
                testName,
                Arrays.asList(),
                Optional.empty(),
                Arrays.asList(new ClassType(java.lang.Exception.class, Span.dummySpan())),
                ListUtils.union(
                        expressionStrategy.env(env, classResolver),
                        actionsAfter.size() > 0
                                ?  Arrays.asList(
                                        new TryStatement(
                                                statements,
                                                Arrays.asList(),
                                                actionsAfter))
                                : statements)));
    }

    Stream<Statement> translateExecutions(List<Execution> executions, String envVarName) {
        String __ = genSym.generate("__");
        return executions.stream()
                .flatMap(execution ->
                        execution.getExpressions()
                                .stream()
                                .flatMap(expression ->
                                        expressionStrategy.eval(
                                                __, expression, Object.class, envVarName).stream()));
    }
}
