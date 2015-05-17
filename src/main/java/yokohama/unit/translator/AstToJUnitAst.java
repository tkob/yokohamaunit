package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import yokohama.unit.ast.AnchorExpr;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.BooleanExpr;
import yokohama.unit.ast.Cell;
import yokohama.unit.ast.CharExpr;
import yokohama.unit.ast.CodeBlock;
import yokohama.unit.ast.CodeBlockExtractVisitor;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.FloatingPointExpr;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.InstanceOfMatcher;
import yokohama.unit.ast.InstanceSuchThatMatcher;
import yokohama.unit.ast.IntegerExpr;
import yokohama.unit.ast.InvocationExpr;
import yokohama.unit.ast.Invoke;
import yokohama.unit.ast.Matcher;
import yokohama.unit.ast.MethodPattern;
import yokohama.unit.ast.NullValueMatcher;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.Predicate;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.StringExpr;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableExtractVisitor;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.Test;
import yokohama.unit.ast_junit.Annotation;
import yokohama.unit.ast_junit.ArrayExpr;
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
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.InvokeStaticVoidStatement;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.LongLitExpr;
import yokohama.unit.ast_junit.Method;
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

@RequiredArgsConstructor
public class AstToJUnitAst {
    final String name;
    final String packageName;
    final ExpressionStrategy expressionStrategy;
    final MockStrategy mockStrategy;
    final GenSym genSym;
    final ClassResolver classResolver;
    final TableExtractVisitor tableExtractVisitor;
    final CodeBlockExtractVisitor codeBlockExtractVisitor =
            new CodeBlockExtractVisitor();

    public CompilationUnit translate(Group group) {
        final List<Table> tables = tableExtractVisitor.extractTables(group);
        Map<String, CodeBlock> codeBlockMap =
                codeBlockExtractVisitor.extractMap(group);
        return new AstToJUnitAstVisitor(
                name,
                packageName,
                expressionStrategy,
                mockStrategy,
                genSym,
                classResolver,
                tables,
                codeBlockMap)
                .translateGroup(group);
    }
}

@AllArgsConstructor
class AstToJUnitAstVisitor {
    final String name;
    final String packageName;
    final ExpressionStrategy expressionStrategy;
    final MockStrategy mockStrategy;
    final GenSym genSym;
    final ClassResolver classResolver;
    final List<Table> tables;
    final Map<String, CodeBlock> codeBlockMap;

    @SneakyThrows(ClassNotFoundException.class)
    private Type MATCHER() {
        return new Type(
                new ClassType(classResolver.lookup("org.hamcrest.Matcher")), 0);
    }

    @SneakyThrows(ClassNotFoundException.class)
    private ClassType CORE_MATCHERS() {
        return new ClassType(classResolver.lookup("org.hamcrest.CoreMatchers"));
    }

    @SneakyThrows(ClassNotFoundException.class)
    private Annotation TEST() {
        return new Annotation(new ClassType(classResolver.lookup("org.junit.Test")));
    }

    CompilationUnit translateGroup(Group group) {
        List<Definition> definitions = group.getDefinitions();
        List<Method> methods =
                definitions.stream()
                        .flatMap(definition -> definition.accept(
                                test -> translateTest(test).stream(),
                                fourPhaseTest ->
                                        translateFourPhaseTest(
                                                fourPhaseTest).stream(),
                                table -> Stream.empty(),
                                codeBlock -> Stream.empty(),
                                heading -> Stream.empty()))
                        .collect(Collectors.toList());
        ClassDecl testClass =
                new ClassDecl(true, name, Optional.empty(), Arrays.asList(), methods);
        Stream<ClassDecl> auxClasses = Stream.concat(
                expressionStrategy.auxClasses(classResolver).stream(),
                mockStrategy.auxClasses().stream());
        List<ClassDecl> classes =
                Stream.concat(auxClasses, Stream.of(testClass))
                        .collect(Collectors.toList());
        return new CompilationUnit(packageName, classes);
    }

    List<Method> translateTest(Test test) {
        final String name = test.getName();
        List<Assertion> assertions = test.getAssertions();
        List<Method> methods = 
                IntStream.range(0, assertions.size())
                        .mapToObj(Integer::new)
                        .flatMap(i ->
                                translateAssertion(
                                        assertions.get(i), i + 1, name)
                                        .stream())
                        .collect(Collectors.toList());
        return methods;
    }

    List<Method> translateAssertion(
            Assertion assertion, int index, String testName) {
        String methodName = SUtils.toIdent(testName) + "_" + index;
        List<Proposition> propositions = assertion.getPropositions();
        return assertion.getFixture().accept(
                () -> {
                    String env = genSym.generate("env");
                    return Arrays.asList(new Method(
                            Arrays.asList(TEST()),
                            methodName,
                            Arrays.asList(),
                            Optional.empty(),
                            Arrays.asList(ClassType.EXCEPTION),
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
                    List<List<Statement>> table =
                            translateTableRef(tableRef, env);
                    return IntStream.range(0, table.size())
                            .mapToObj(Integer::new)
                            .map(i -> {
                                return new Method(
                                        Arrays.asList(TEST()),
                                        methodName + "_" + (i + 1),
                                        Arrays.asList(),
                                        Optional.empty(),
                                        Arrays.asList(ClassType.EXCEPTION),
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
                            Arrays.asList(TEST()),
                            methodName,
                            Arrays.asList(),
                            Optional.empty(),
                            Arrays.asList(ClassType.EXCEPTION),
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

    Stream<Statement> translateProposition(
            Proposition proposition, String envVarName) {
        String actual = genSym.generate("actual");
        String expected = genSym.generate("expected");
        Predicate predicate = proposition.getPredicate();
        Stream<Statement> subjectAndPredicate = predicate.<Stream<Statement>>accept(
                isPredicate -> {
                    return Stream.concat(
                            translateExpr(
                                    proposition.getSubject(),
                                    actual,
                                    Object.class,
                                    envVarName),
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
                            translateExpr(
                                    proposition.getSubject(),
                                    actual,
                                    Object.class,
                                    envVarName),
                            Stream.concat(
                                    translateMatcher(
                                            isNotPredicate.getComplement(),
                                            unexpected,
                                            actual,
                                            envVarName),
                                    Stream.of(new VarInitStatement(
                                            MATCHER(),
                                            expected,
                                            new InvokeStaticExpr(
                                                    CORE_MATCHERS(),
                                                    Arrays.asList(),
                                                    "not",
                                                    Arrays.asList(MATCHER()),
                                                    Arrays.asList(new Var(unexpected)),
                                                    MATCHER()),
                                            predicate.getSpan()))));
                },
                throwsPredicate -> {
                    String __ = genSym.generate("tmp");
                    return Stream.concat(
                            bindThrown(
                                    actual,
                                    translateExpr(
                                            proposition.getSubject(),
                                            __,
                                            Object.class,
                                            envVarName)
                                            .collect(Collectors.toList()),
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
                        : Stream.of(new IsStatement(
                                new Var(actual),
                                new Var(expected),
                                predicate.getSpan())));
    }

    Stream<Statement> bindThrown(
            String actual, List<Statement> statements, String envVarName) {
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
                                        Type.THROWABLE,
                                        actual,
                                        new NullExpr(),
                                        Span.dummySpan()))),
                        Stream.concat(
                                Optionals.toStream(
                                        expressionStrategy.catchAndAssignCause(actual)),
                                Stream.of(new CatchClause(
                                        ClassType.THROWABLE,
                                        new Var(e),
                                        Arrays.asList(new VarInitStatement(
                                                Type.THROWABLE,
                                                actual,
                                                new VarExpr(e),
                                                Span.dummySpan())))))
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
                        translateExpr(
                                equalTo.getExpr(),
                                objVar.getName(),
                                Object.class,
                                envVarName),
                        Stream.of(new VarInitStatement(
                                MATCHER(),
                                varName,
                                new EqualToMatcherExpr(objVar),
                                equalTo.getSpan())));
            },
            (InstanceOfMatcher instanceOf) -> {
                return Stream.of(new VarInitStatement(
                        MATCHER(),
                        varName,
                        new InstanceOfMatcherExpr(
                                lookupClassName(
                                        instanceOf.getClazz().getName(),
                                        instanceOf.getSpan())),
                        instanceOf.getSpan()));
            },
            (InstanceSuchThatMatcher instanceSuchThat) -> {
                Ident bindVar = instanceSuchThat.getVar();
                yokohama.unit.ast.ClassType clazz = instanceSuchThat.getClazz();
                List<Proposition> propositions = instanceSuchThat.getPropositions();
                Span span = instanceSuchThat.getSpan();

                String instanceOfVarName = genSym.generate("instanceOfMatcher");
                Stream<Statement> instanceOfStatements = Stream.of(
                        new VarInitStatement(
                                MATCHER(),
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
                        expressionStrategy.bind(envVarName, bindVar, new Var(actual)).stream();

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
                                MATCHER(),
                                varName,
                                new NullValueMatcherExpr(),
                                nullValue.getSpan()));
            });
    }

    Stream<Statement> translateBinding(
            yokohama.unit.ast.Binding binding, String envVarName) {
        Ident name = binding.getName();
        String varName = genSym.generate(name.getName());
        return Stream.concat(
                translateExpr(
                        binding.getValue(), varName, Object.class, envVarName),
                expressionStrategy.bind(
                        envVarName, name, new Var(varName)).stream());
    }

    Stream<Statement> translateExpr(
            yokohama.unit.ast.Expr expr,
            String varName,
            Class<?> expectedType,
            String envVarName) {
        Var exprVar = new Var(genSym.generate("expr"));
        Stream<Statement> statements = expr.accept(
                quotedExpr -> {
                    return expressionStrategy.eval(
                            exprVar.getName(),
                            quotedExpr,
                            Type.fromClass(expectedType).box().toClass(),
                            envVarName).stream();
                },
                stubExpr -> {
                    return mockStrategy.stub(
                            exprVar.getName(),
                            stubExpr,
                            this,
                            envVarName).stream();
                },
                invocationExpr -> {
                    return translateInvocationExpr(
                            invocationExpr, exprVar.getName(), envVarName);
                },
                integerExpr -> {
                    return translateIntegerExpr(
                            integerExpr, exprVar.getName(), envVarName);
                },
                floatingPointExpr -> {
                    return translateFloatingPointExpr(
                            floatingPointExpr, exprVar.getName(), envVarName);
                },
                booleanExpr -> {
                    return translateBooleanExpr(
                            booleanExpr, exprVar.getName(), envVarName);
                },
                charExpr -> {
                    return translateCharExpr(
                            charExpr, exprVar.getName(), envVarName);
                },
                stringExpr -> {
                    return translateStringExpr(
                            stringExpr, exprVar.getName(), envVarName);
                },
                anchorExpr -> {
                    return translateAnchorExpr(
                            anchorExpr, exprVar.getName(), envVarName);
                });

        // box or unbox if needed
        Stream<Statement> boxing =
                boxOrUnbox(
                        Type.fromClass(expectedType),
                        varName,
                        typeOfExpr(expr),
                        exprVar);

        return Stream.concat(statements, boxing);
    }

    Stream<Statement> translateInvocationExpr(
            InvocationExpr invocationExpr,
            String varName,
            String envVarName) {
        yokohama.unit.ast.ClassType classType = invocationExpr.getClassType();
        Class<?> clazz = classType.toClass(classResolver);
        MethodPattern methodPattern = invocationExpr.getMethodPattern();
        String methodName = methodPattern.getName();
        List<yokohama.unit.ast.Type> argTypes = methodPattern.getParamTypes();
        boolean isVararg = methodPattern.isVararg();
        Optional<yokohama.unit.ast.Expr> receiver = invocationExpr.getReceiver();
        List<yokohama.unit.ast.Expr> args = invocationExpr.getArgs();

        Type returnType = typeOfExpr(invocationExpr);

        Pair<List<Var>, Stream<Statement>> argVarsAndStatements =
                setupArgs(argTypes, isVararg, args, envVarName);
        List<Var> argVars = argVarsAndStatements.getFirst();
        Stream<Statement> setupStatements = argVarsAndStatements.getSecond();

        Stream<Statement> invocation = generateInvoke(
                varName,
                classType,
                methodName,
                argTypes,
                isVararg,
                argVars,
                receiver,
                returnType,
                envVarName,
                invocationExpr.getSpan());

        return Stream.concat(setupStatements, invocation);
    }

    Stream<Statement> translateIntegerExpr(
            IntegerExpr integerExpr,
            String varName,
            String envVarName) {
        return integerExpr.match(
                intValue -> {
                    return Stream.<Statement>of(
                            new VarInitStatement(
                                    Type.INT,
                                    varName,
                                    new IntLitExpr(intValue),
                                    integerExpr.getSpan()));
                },
                longValue -> {
                    return Stream.<Statement>of(
                            new VarInitStatement(
                                    Type.LONG,
                                    varName,
                                    new LongLitExpr(longValue),
                                    integerExpr.getSpan()));
                });
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

    Stream<Statement> translateAnchorExpr(
            AnchorExpr anchorExpr, String varName, String envVarName) {
        String anchor = anchorExpr.getAnchor();
        CodeBlock codeBlock = codeBlockMap.get(anchor);
        String code = codeBlock.getCode();
        return Stream.<Statement>of(
                new VarInitStatement(
                        Type.STRING,
                        varName,
                        new StrLitExpr(code),
                        anchorExpr.getSpan()));
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
                    ClassType boxType;
                    String unboxMethodName;
                    switch (primitiveType.getKind()) {
                        case BOOLEAN:
                            boxType = primitiveType.box();
                            unboxMethodName = "booleanValue";
                            break;
                        case BYTE:
                            boxType = ClassType.fromClass(Number.class);
                            unboxMethodName = "byteValue";
                            break;
                        case SHORT:
                            boxType = ClassType.fromClass(Number.class);
                            unboxMethodName = "shortValue";
                            break;
                        case INT:
                            boxType = ClassType.fromClass(Number.class);
                            unboxMethodName = "intValue";
                            break;
                        case LONG:
                            boxType = ClassType.fromClass(Number.class);
                            unboxMethodName = "longValue";
                            break;
                        case CHAR:
                            boxType = primitiveType.box();
                            unboxMethodName = "charValue";
                            break;
                        case FLOAT:
                            boxType = ClassType.fromClass(Number.class);
                            unboxMethodName = "floatValue";
                            break;
                        case DOUBLE:
                            boxType = ClassType.fromClass(Number.class);
                            unboxMethodName = "doubleValue";
                            break;
                        default:
                            throw new RuntimeException("should not reach here");
                    }
                    return Stream.of(
                            new VarInitStatement(
                                    boxType.toType(),
                                    boxVar.getName(),
                                    new VarExpr(fromVar.getName()),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    toType,
                                    toVarName,
                                    new InvokeExpr(
                                            boxType,
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

    private Type typeOfExpr(yokohama.unit.ast.Expr expr) {
        return expr.accept(
                quotedExpr -> Type.OBJECT,
                stubExpr -> Type.of(
                        stubExpr.getClassToStub().toType(), classResolver),
                invocationExpr -> {
                    MethodPattern mp = invocationExpr.getMethodPattern();
                    return mp.getReturnType(
                            invocationExpr.getClassType(),
                            classResolver)
                            .map(type -> Type.of(type, classResolver))
                            .get();
                },
                integerExpr -> integerExpr.match(
                        intValue -> Type.INT,
                        longValue -> Type.LONG),
                floatingPointExpr -> floatingPointExpr.match(
                        floatValue -> Type.FLOAT,
                        doubleValue -> Type.DOUBLE),
                booleanExpr -> Type.BOOLEAN,
                charExpr -> Type.CHAR,
                stringExpr -> Type.STRING,
                anchorExpr -> Type.STRING);
    }

    List<List<Statement>> translateTableRef(
            TableRef tableRef,
            String envVarName) {
        String name = tableRef.getName();
        List<Ident> idents = tableRef.getIdents();
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
                    return parseCSV(
                            name, CSVFormat.DEFAULT.withHeader(), idents, envVarName);
                case TSV:
                    return parseCSV(
                            name, CSVFormat.TDF.withHeader(), idents, envVarName);
                case EXCEL:
                    return parseExcel(name, idents, envVarName);
            }
            throw new IllegalArgumentException(
                    "'" + Objects.toString(tableRef) + "' is not a table reference.");
        } catch (InvalidFormatException | IOException e) {
            throw new TranslationException(e.getMessage(), tableRef.getSpan(), e);
        }

    }

    List<List<Statement>> translateTable(
            Table table,
            List<Ident> idents,
            String envVarName) {
        return table.getRows()
                .stream()
                .map(row ->
                        translateRow(row, table.getHeader(), idents, envVarName))
                .collect(Collectors.toList());
    }

    List<Statement> translateRow(
            Row row,
            List<Ident> header,
            List<Ident> idents,
            String envVarName) {
        return IntStream.range(0, header.size())
                .filter(i -> idents.contains(header.get(i)))
                .mapToObj(Integer::new)
                .flatMap(i -> {
                    Cell cell = row.getCells().get(i);
                    return cell.accept(
                            exprCell -> {
                                String varName = genSym.generate(header.get(i).getName());
                                return Stream.concat(
                                        translateExpr(
                                                exprCell.getExpr(), varName, Object.class, envVarName),
                                        expressionStrategy.bind(
                                                envVarName, header.get(i), new Var(varName)).stream());
                            },
                            predCell -> {
                                throw new TranslationException(
                                        "Expected expression but found predicate",
                                        predCell.getSpan());
                            });
                })
                .collect(Collectors.toList());
    }

    List<List<Statement>> parseCSV(
            String fileName, CSVFormat format, List<Ident> idents, String envVarName)
            throws IOException {
        Map<String, Ident> nameToIdent = idents.stream()
                .collect(() -> new TreeMap<>(),
                        (map, ident) -> map.put(ident.getName(), ident),
                        (m1, m2) -> m1.putAll(m2));
        try (   final InputStream in = getClass().getResourceAsStream(fileName);
                final Reader reader = new InputStreamReader(in, "UTF-8");
                final CSVParser parser = new CSVParser(reader, format)) {
            return StreamSupport.stream(parser.spliterator(), false)
                    .map(record ->
                            parser.getHeaderMap().keySet()
                                    .stream()
                                    .filter(key -> idents.stream().anyMatch(ident -> ident.getName().equals(key)))
                                    .map(key -> nameToIdent.get(key))
                                    .flatMap(ident -> {
                                        String varName = genSym.generate(ident.getName());
                                        return Stream.concat(expressionStrategy.eval(varName,
                                                new yokohama.unit.ast.QuotedExpr(
                                                        record.get(ident.getName()),
                                                        new yokohama.unit.position.Span(
                                                                Optional.of(Paths.get(fileName)),
                                                                new Position((int)parser.getCurrentLineNumber(), -1),
                                                                new Position(-1, -1))),
                                                Object.class, envVarName).stream(),
                                                expressionStrategy.bind(envVarName, ident, new Var(varName)).stream());
                                    })
                                    .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<List<Statement>> parseExcel(
            String fileName, List<Ident> idents, String envVarName)
            throws InvalidFormatException, IOException {
        Map<String, Ident> nameToIdent = idents.stream()
                .collect(() -> new TreeMap<>(),
                        (map, ident) -> map.put(ident.getName(), ident),
                        (m1, m2) -> m1.putAll(m2));
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
                                .filter(i -> idents.stream().anyMatch(ident -> ident.getName().equals(names.get(i))))
                                .mapToObj(Integer::new)
                                .flatMap(i -> {
                                    Ident ident = nameToIdent.get(names.get(i));
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
                                            expressionStrategy.bind(envVarName, ident, new Var(varName)).stream());
                                })
                                .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<Method> translateFourPhaseTest(FourPhaseTest fourPhaseTest) {
        String env = genSym.generate("env");

        String testName = SUtils.toIdent(fourPhaseTest.getName());
        Stream<Statement> bindings;
        if (fourPhaseTest.getSetup().isPresent()) {
            Phase setup = fourPhaseTest.getSetup().get();
            bindings = setup.getLetStatements().stream()
                    .flatMap(letStatement ->
                            letStatement.getBindings().stream()
                                    .flatMap(binding -> {
                                        String varName =
                                                genSym.generate(binding.getName().getName());
                                        return Stream.concat(
                                                translateExpr(
                                                        binding.getValue(),
                                                        varName,
                                                        Object.class,
                                                        env),
                                                expressionStrategy.bind(
                                                        env,
                                                        binding.getName(),
                                                        new Var(varName))
                                                        .stream());
                                    }));
        } else {
            bindings = Stream.empty();
        }

        Optional<Stream<Statement>> setupActions =
                fourPhaseTest.getSetup()
                        .map(Phase::getStatements)
                        .map(statements -> translateStatements(statements, env));
        Optional<Stream<Statement>> exerciseActions =
                fourPhaseTest.getExercise()
                        .map(Phase::getStatements)
                        .map(statements -> translateStatements(statements, env));
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
                                        setupActions.isPresent()
                                                ? setupActions.get()
                                                : Stream.empty(),
                                        exerciseActions.isPresent()
                                                ? exerciseActions.get()
                                                : Stream.empty()),
                                testStatements)
                ).collect(Collectors.toList());


        List<Statement> actionsAfter;
        if (fourPhaseTest.getTeardown().isPresent()) {
            Phase teardown = fourPhaseTest.getTeardown().get();
            actionsAfter =
                    translateStatements(teardown.getStatements(), env)
                            .collect(Collectors.toList());
        } else {
            actionsAfter = Arrays.asList();
        }

        return Arrays.asList(new Method(
                Arrays.asList(TEST()),
                testName,
                Arrays.asList(),
                Optional.empty(),
                Arrays.asList(ClassType.EXCEPTION),
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

    Stream<Statement> translateStatements(
            List<yokohama.unit.ast.Statement> statements, String envVarName) {
        return statements.stream()
                .flatMap(statement -> statement.accept(
                        execution -> translateExecution(execution, envVarName),
                        invoke -> translateInvoke(invoke, envVarName)));
    }

    Stream<Statement> translateExecution(
            Execution execution, String envVarName) {
        String __ = genSym.generate("__");
        return execution.getExpressions().stream()
                .flatMap(expression ->
                        expressionStrategy.eval(
                                __, expression, Object.class, envVarName).stream());
    }

    Stream<Statement> translateInvoke(Invoke invoke, String envVarName) {
        yokohama.unit.ast.ClassType classType = invoke.getClassType();
        Class<?> clazz = classType.toClass(classResolver);
        MethodPattern methodPattern = invoke.getMethodPattern();
        String methodName = methodPattern.getName();
        List<yokohama.unit.ast.Type> argTypes = methodPattern.getParamTypes();
        boolean isVararg = methodPattern.isVararg();
        Optional<yokohama.unit.ast.Expr> receiver = invoke.getReceiver();
        List<yokohama.unit.ast.Expr> args = invoke.getArgs();

        Pair<List<Var>, Stream<Statement>> argVarsAndStatements =
                setupArgs(argTypes, isVararg, args, envVarName);
        List<Var> argVars = argVarsAndStatements.getFirst();
        Stream<Statement> setupStatements = argVarsAndStatements.getSecond();

        Optional<Type> returnType = invoke.getMethodPattern()
                .getReturnType(classType, classResolver)
                .map(type -> Type.of(type, classResolver));
        Stream<Statement> invocation;
        if (returnType.isPresent()) {
            invocation = generateInvoke(
                    genSym.generate("__"),
                    classType,
                    methodName,
                    argTypes,
                    isVararg,
                    argVars,
                    receiver,
                    returnType.get(),
                    envVarName,
                    invoke.getSpan());
        } else {
            invocation = generateInvokeVoid(
                    classType,
                    methodName,
                    argTypes,
                    isVararg,
                    argVars,
                    receiver,
                    envVarName,
                    invoke.getSpan());
        }

        return Stream.concat(setupStatements, invocation);
    }

    Pair<List<Var>, Stream<Statement>> setupArgs(
            List<yokohama.unit.ast.Type> argTypes,
            boolean isVararg,
            List<yokohama.unit.ast.Expr> args,
            String envVarName) {
        List<Pair<Var, Stream<Statement>>> setupArgs;
        if (isVararg) {
            int splitAt = argTypes.size() - 1;
            List<Pair<yokohama.unit.ast.Type, List<yokohama.unit.ast.Expr>>> typesAndArgs = 
                    Pair.zip(
                            argTypes,
                            Lists.split(args, splitAt).map((nonVarargs, varargs) ->
                                    ListUtils.union(
                                            Lists.map(nonVarargs, Arrays::asList),
                                            Arrays.asList(varargs))));
            setupArgs = Lists.mapInitAndLast(
                    typesAndArgs,
                    typeAndArg -> typeAndArg.map((t, arg) -> {
                        Var argVar = new Var(genSym.generate("arg"));
                        Type paramType = Type.of(t, classResolver);
                        Stream<Statement> expr = translateExpr(
                                arg.get(0), argVar.getName(), paramType.toClass(), envVarName);
                        return new Pair<>(argVar, expr);
                    }),
                    typeAndArg -> typeAndArg.map((t, varargs) -> {
                        Type paramType = Type.of(t, classResolver);
                        List<Pair<Var, Stream<Statement>>> exprs = varargs.stream().map(
                                vararg -> {
                                    Var varargVar = new Var(genSym.generate("vararg"));
                                    Stream<Statement> expr = translateExpr(
                                            vararg,
                                            varargVar.getName(),
                                            paramType.toClass(),
                                            envVarName);
                                    return new Pair<>(varargVar, expr);
                                }).collect(Collectors.toList());
                        List<Var> varargVars = Pair.unzip(exprs).getFirst();
                        Stream<Statement> varargStatements = exprs.stream().flatMap(Pair::getSecond);
                        Var argVar = new Var(genSym.generate("arg"));
                        Stream<Statement> arrayStatement = Stream.of(
                                new VarInitStatement(
                                        paramType.toArray(),
                                        argVar.getName(),
                                        new ArrayExpr(paramType.toArray(), varargVars),
                                        t.getSpan()));
                        return new Pair<>(argVar, Stream.concat(varargStatements, arrayStatement));
                    }));
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

        return new Pair<>(argVars, setupStatements);
    }

    Stream<Statement> generateInvoke(
            String varName,
            yokohama.unit.ast.ClassType classType,
            String methodName,
            List<yokohama.unit.ast.Type> argTypes,
            boolean isVararg,
            List<Var> argVars,
            Optional<yokohama.unit.ast.Expr> receiver,
            Type returnType,
            String envVarName,
            Span span) {
        Class<?> clazz = classType.toClass(classResolver);

        if (receiver.isPresent()) {
            // invokevirtual
            Var receiverVar = new Var(genSym.generate("receiver"));
            Stream<Statement> getReceiver = translateExpr(
                    receiver.get(),receiverVar.getName(), clazz, envVarName);
            return Stream.concat(getReceiver, Stream.of(
                    new VarInitStatement(
                            returnType,
                            varName,
                            new InvokeExpr(
                                    ClassType.of(classType, classResolver),
                                    receiverVar,
                                    methodName,
                                    Lists.mapInitAndLast(
                                            Type.listOf(argTypes, classResolver),
                                            type -> type,
                                            type -> isVararg ? type.toArray(): type),
                                    argVars,
                                    returnType),
                            span)));
        } else {
            // invokestatic
            return Stream.of(
                    new VarInitStatement(
                            returnType,
                            varName,
                            new InvokeStaticExpr(
                                    ClassType.of(classType, classResolver),
                                    Collections.emptyList(),
                                    methodName,
                                    Lists.mapInitAndLast(
                                            Type.listOf(argTypes, classResolver),
                                            type -> type,
                                            type -> isVararg ? type.toArray(): type),
                                    argVars,
                                    returnType),
                            span));
        }
    }

    Stream<Statement> generateInvokeVoid(
            yokohama.unit.ast.ClassType classType,
            String methodName,
            List<yokohama.unit.ast.Type> argTypes,
            boolean isVararg,
            List<Var> argVars,
            Optional<yokohama.unit.ast.Expr> receiver,
            String envVarName,
            Span span) {
        Class<?> clazz = classType.toClass(classResolver);

        if (receiver.isPresent()) {
            // invokevirtual
            Var receiverVar = new Var(genSym.generate("receiver"));
            Stream<Statement> getReceiver = translateExpr(
                    receiver.get(), receiverVar.getName(), clazz, envVarName);
            return Stream.concat(getReceiver, Stream.of(
                    new InvokeVoidStatement(
                            ClassType.of(classType, classResolver),
                            receiverVar,
                            methodName,
                            Lists.mapInitAndLast(
                                    Type.listOf(argTypes, classResolver),
                                    type -> type,
                                    type -> isVararg ? type.toArray(): type),
                            argVars,
                            span)));
        } else {
            // invokestatic
            return Stream.of(
                    new InvokeStaticVoidStatement(
                            ClassType.of(classType, classResolver),
                            Collections.emptyList(),
                            methodName,
                            Lists.mapInitAndLast(
                                    Type.listOf(argTypes, classResolver),
                                    type -> type,
                                    type -> isVararg ? type.toArray(): type),
                            argVars,
                            span));
        }
    }
}
