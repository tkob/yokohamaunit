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
import yokohama.unit.util.Sym;
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

    static final String MATCHER = "org.hamcrest.Matcher";
    static final String CORE_MATCHERS = "org.hamcrest.CoreMatchers";
    static final String TEST = "org.junit.Test";

    @SneakyThrows(ClassNotFoundException.class)
    ClassType classTypeOf(String name) {
        return new ClassType(classResolver.lookup(name));
    }
    Type typeOf(String name) {
        return classTypeOf(name).toType();
    }
    Annotation annotationOf(String name) {
        return new Annotation(classTypeOf(name));
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
                expressionStrategy.auxClasses().stream(),
                mockStrategy.auxClasses().stream());
        List<ClassDecl> classes =
                Stream.concat(auxClasses, Stream.of(testClass))
                        .collect(Collectors.toList());
        return new CompilationUnit(packageName, classes);
    }

    List<Method> translateTest(Test test) {
        final String name = test.getName();
        List<Assertion> assertions = test.getAssertions();
        return IntStream.range(0, assertions.size())
                .mapToObj(Integer::new)
                .flatMap(assertionNo -> {
                    Sym env = genSym.generate("env");
                    List<List<Statement>> bodies =
                            translateAssertion(
                                    assertions.get(assertionNo), env);
                    return IntStream.range(0, bodies.size())
                            .mapToObj(Integer::new)
                            .map(bodyNo -> {
                                String methodName = SUtils.toIdent(name)
                                        + "_" + assertionNo + "_" + bodyNo;
                                return new Method(
                                        Arrays.asList(annotationOf(TEST)),
                                        methodName,
                                        Arrays.asList(),
                                        Optional.empty(),
                                        Arrays.asList(ClassType.EXCEPTION),
                                        ListUtils.union(
                                                expressionStrategy.env(env),
                                                bodies.get(bodyNo)));
                            });
                })
                .collect(Collectors.toList());
    }

    List<List<Statement>> translateAssertion(Assertion assertion, Sym env) {
        List<Proposition> propositions = assertion.getPropositions();
        return assertion.getFixture().accept(
                () -> {
                    List<Statement> body = propositions.stream()
                            .flatMap(proposition ->
                                    translateProposition(proposition, env))
                            .collect(Collectors.toList());
                    return Arrays.asList(body);
                },
                tableRef -> {
                    List<List<Statement>> table =
                            translateTableRef(tableRef, env);
                    return IntStream.range(0, table.size())
                            .mapToObj(Integer::new)
                            .map(i -> {
                                return ListUtils.union(
                                        table.get(i),
                                        propositions.stream()
                                                .flatMap(proposition ->
                                                        translateProposition(proposition, env))
                                                .collect(Collectors.toList()));
                            }).collect(Collectors.toList());
                },
                bindings -> {
                    List<Statement> body = Stream.concat(
                            bindings.getBindings()
                                    .stream()
                                    .flatMap(binding ->
                                            translateBinding(binding, env)),
                            propositions.stream()
                                    .flatMap(proposition ->
                                            translateProposition(proposition, env)))
                            .collect(Collectors.toList());
                    return Arrays.asList(body);
                });
    }

    Stream<Statement> translateProposition(
            Proposition proposition, Sym envVar) {
        Sym actual = genSym.generate("actual");
        Sym expected = genSym.generate("expected");
        Predicate predicate = proposition.getPredicate();
        Stream<Statement> subjectAndPredicate = predicate.<Stream<Statement>>accept(isPredicate -> {
                    return Stream.concat(
                            translateExpr(
                                    proposition.getSubject(),
                                    actual,
                                    Object.class,
                                    envVar),
                            translateMatcher(
                                    isPredicate.getComplement(),
                                    expected,
                                    actual,
                                    envVar));
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
                    Sym unexpected = genSym.generate("unexpected");
                    return Stream.concat(translateExpr(
                                    proposition.getSubject(),
                                    actual,
                                    Object.class,
                                    envVar),
                            Stream.concat(translateMatcher(
                                            isNotPredicate.getComplement(),
                                            unexpected,
                                            actual,
                                            envVar),
                                    Stream.of(new VarInitStatement(
                                            typeOf(MATCHER),
                                            expected,
                                            new InvokeStaticExpr(
                                                    classTypeOf(CORE_MATCHERS),
                                                    Arrays.asList(),
                                                    "not",
                                                    Arrays.asList(typeOf(MATCHER)),
                                                    Arrays.asList(unexpected),
                                                    typeOf(MATCHER)),
                                            predicate.getSpan()))));
                },
                throwsPredicate -> {
                    Sym __ = genSym.generate("tmp");
                    return Stream.concat(
                            bindThrown(
                                    actual,
                                    translateExpr(
                                            proposition.getSubject(),
                                            __,
                                            Object.class,
                                            envVar)
                                            .collect(Collectors.toList()),
                                    envVar),
                            translateMatcher(
                                    throwsPredicate.getThrowee(),
                                    expected,
                                    actual,
                                    envVar));
                }
        );
        Matcher matcher = predicate.accept(
                isPredicate -> isPredicate.getComplement(),
                isNotPredicate -> isNotPredicate.getComplement(),
                throwsPredicate -> throwsPredicate.getThrowee());
        return Stream.concat(subjectAndPredicate,
                matcher instanceof InstanceSuchThatMatcher
                        ? Stream.empty()
                        : Stream.of(new IsStatement(
                                actual,
                                expected,
                                predicate.getSpan())));
    }

    Stream<Statement> bindThrown(
            Sym actual, List<Statement> statements, Sym envVar) {
        Sym e = genSym.generate("ex");
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
        return Stream.of(new TryStatement(
                        ListUtils.union(
                                statements,
                                Arrays.asList(new VarInitStatement(
                                        Type.THROWABLE,
                                        actual,
                                        new NullExpr(),
                                        Span.dummySpan()))),
                        Stream.concat(Optionals.toStream(
                                        expressionStrategy.catchAndAssignCause(actual)),
                                Stream.of(new CatchClause(
                                        ClassType.THROWABLE,
                                        e,
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
            Sym var,
            Sym actual,
            Sym envVar) {
        return matcher.<Stream<Statement>>accept((EqualToMatcher equalTo) -> {
                Sym objVar = genSym.generate("obj");
                return Stream.concat(translateExpr(equalTo.getExpr(),
                                objVar,
                                Object.class,
                                envVar),
                        Stream.of(new VarInitStatement(
                                typeOf(MATCHER),
                                var,
                                new EqualToMatcherExpr(objVar),
                                equalTo.getSpan())));
            },
            (InstanceOfMatcher instanceOf) -> {
                return Stream.of(new VarInitStatement(
                        typeOf(MATCHER),
                        var,
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

                Sym instanceOfVar = genSym.generate("instanceOfMatcher");
                Stream<Statement> instanceOfStatements = Stream.of(new VarInitStatement(
                                typeOf(MATCHER),
                                instanceOfVar,
                                new InstanceOfMatcherExpr(
                                        lookupClassName(
                                                clazz.getName(),
                                                clazz.getSpan())),
                                clazz.getSpan()),
                        new IsStatement(
                                actual,
                                instanceOfVar,
                                clazz.getSpan()));

                Stream<Statement> bindStatements =
                        expressionStrategy.bind(envVar, bindVar, actual).stream();

                Stream<Statement> suchThatStatements =
                        propositions
                                .stream()
                                .flatMap(proposition ->
                                        translateProposition(proposition, envVar));

                return Stream.concat(
                        instanceOfStatements,
                        Stream.concat(
                                bindStatements,
                                suchThatStatements));
            },
            (NullValueMatcher nullValue) -> {
                return Stream.of(new VarInitStatement(
                                typeOf(MATCHER),
                                var,
                                new NullValueMatcherExpr(),
                                nullValue.getSpan()));
            });
    }

    Stream<Statement> translateBinding(
            yokohama.unit.ast.Binding binding, Sym envVar) {
        Ident name = binding.getName();
        Sym var = genSym.generate(name.getName());
        return Stream.concat(translateExpr(binding.getValue(), var, Object.class, envVar),
                expressionStrategy.bind(envVar, name, var).stream());
    }

    Stream<Statement> translateExpr(
            yokohama.unit.ast.Expr expr,
            Sym var,
            Class<?> expectedType,
            Sym envVar) {
        Sym exprVar = genSym.generate("expr");
        Stream<Statement> statements = expr.accept(quotedExpr -> {
                    return expressionStrategy.eval(
                            exprVar,
                            quotedExpr,
                            Type.fromClass(expectedType).box().toClass(),
                            envVar).stream();
                },
                stubExpr -> {
                    return mockStrategy.stub(
                            exprVar,
                            stubExpr,
                            this,
                            envVar).stream();
                },
                invocationExpr -> {
                    return translateInvocationExpr(invocationExpr, exprVar, envVar);
                },
                integerExpr -> {
                    return translateIntegerExpr(integerExpr, exprVar, envVar);
                },
                floatingPointExpr -> {
                    return translateFloatingPointExpr(floatingPointExpr, exprVar, envVar);
                },
                booleanExpr -> {
                    return translateBooleanExpr(booleanExpr, exprVar, envVar);
                },
                charExpr -> {
                    return translateCharExpr(charExpr, exprVar, envVar);
                },
                stringExpr -> {
                    return translateStringExpr(stringExpr, exprVar, envVar);
                },
                anchorExpr -> {
                    return translateAnchorExpr(anchorExpr, exprVar, envVar);
                });

        // box or unbox if needed
        Stream<Statement> boxing =
                boxOrUnbox(Type.fromClass(expectedType),
                        var,
                        typeOfExpr(expr),
                        exprVar);

        return Stream.concat(statements, boxing);
    }

    Stream<Statement> translateInvocationExpr(
            InvocationExpr invocationExpr,
            Sym var,
            Sym envVar) {
        yokohama.unit.ast.ClassType classType = invocationExpr.getClassType();
        Class<?> clazz = classType.toClass(classResolver);
        MethodPattern methodPattern = invocationExpr.getMethodPattern();
        String methodName = methodPattern.getName();
        List<yokohama.unit.ast.Type> argTypes = methodPattern.getParamTypes();
        boolean isVararg = methodPattern.isVararg();
        Optional<yokohama.unit.ast.Expr> receiver = invocationExpr.getReceiver();
        List<yokohama.unit.ast.Expr> args = invocationExpr.getArgs();

        Type returnType = typeOfExpr(invocationExpr);

        Pair<List<Sym>, Stream<Statement>> argVarsAndStatements =
                setupArgs(argTypes, isVararg, args, envVar);
        List<Sym> argVars = argVarsAndStatements.getFirst();
        Stream<Statement> setupStatements = argVarsAndStatements.getSecond();

        Stream<Statement> invocation = generateInvoke(
                var,
                classType,
                methodName,
                argTypes,
                isVararg,
                argVars,
                receiver,
                returnType,
                envVar,
                invocationExpr.getSpan());

        return Stream.concat(setupStatements, invocation);
    }

    Stream<Statement> translateIntegerExpr(
            IntegerExpr integerExpr,
            Sym var,
            Sym envVar) {
        return integerExpr.match(intValue -> {
                    return Stream.<Statement>of(new VarInitStatement(
                                    Type.INT,
                                    var,
                                    new IntLitExpr(intValue),
                                    integerExpr.getSpan()));
                },
                longValue -> {
                    return Stream.<Statement>of(new VarInitStatement(
                                    Type.LONG,
                                    var,
                                    new LongLitExpr(longValue),
                                    integerExpr.getSpan()));
                });
    }

    Stream<Statement> translateFloatingPointExpr(
            FloatingPointExpr floatingPointExpr,
            Sym var,
            Sym envVar) {
        return floatingPointExpr.match(floatValue -> {
                    return Stream.<Statement>of(new VarInitStatement(
                                    Type.FLOAT,
                                    var,
                                    new FloatLitExpr(floatValue),
                                    floatingPointExpr.getSpan()));
                },
                doubleValue -> {
                    return Stream.<Statement>of(new VarInitStatement(
                                    Type.DOUBLE,
                                    var,
                                    new DoubleLitExpr(doubleValue),
                                    floatingPointExpr.getSpan()));
                });
    }

    Stream<Statement> translateBooleanExpr(
            BooleanExpr booleanExpr, Sym var, Sym envVar) {
        boolean booleanValue = booleanExpr.getValue();
        return Stream.<Statement>of(new VarInitStatement(
                        Type.BOOLEAN,
                        var,
                        new BooleanLitExpr(booleanValue),
                        booleanExpr.getSpan()));
    }

    Stream<Statement> translateCharExpr(
            CharExpr charExpr, Sym var, Sym envVar) {
        char charValue = charExpr.getValue();
        return Stream.<Statement>of(new VarInitStatement(
                        Type.CHAR,
                        var,
                        new CharLitExpr(charValue),
                        charExpr.getSpan()));
    }

    Stream<Statement> translateStringExpr(
            StringExpr stringExpr, Sym var, Sym envVar) {
        String strValue = stringExpr.getValue();
        return Stream.<Statement>of(new VarInitStatement(
                        Type.STRING,
                        var,
                        new StrLitExpr(strValue),
                        stringExpr.getSpan()));
    }

    Stream<Statement> translateAnchorExpr(
            AnchorExpr anchorExpr, Sym var, Sym envVar) {
        String anchor = anchorExpr.getAnchor();
        CodeBlock codeBlock = codeBlockMap.get(anchor);
        String code = codeBlock.getCode();
        return Stream.<Statement>of(new VarInitStatement(
                        Type.STRING,
                        var,
                        new StrLitExpr(code),
                        anchorExpr.getSpan()));
    }

    Stream<Statement> boxOrUnbox(
            Type toType, Sym toVar, Type fromType, Sym fromVar) {
        return fromType.<Stream<Statement>>matchPrimitiveOrNot(
                primitiveType -> {
                    return fromPrimitive(
                            toType, toVar, primitiveType, fromVar);
                },
                nonPrimitiveType -> {
                    return fromNonPrimtive(toType, toVar, fromVar);
                });
    }
    
    private Stream<Statement> fromPrimitive(
            Type toType, Sym toVar, PrimitiveType fromType, Sym fromVar) {
        return toType.<Stream<Statement>>matchPrimitiveOrNot(
                primitiveType -> {
                    return Stream.of(
                            new VarInitStatement(
                                    toType,
                                    toVar,
                                    new VarExpr(fromVar),
                                    Span.dummySpan()));
                },
                nonPrimitiveType -> {
                    return Stream.of(new VarInitStatement(
                            nonPrimitiveType,
                            toVar,
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
            Type toType, Sym toVar, Sym fromVar) {
        // precondition: fromVar is not primitive
        return toType.<Stream<Statement>>matchPrimitiveOrNot(primitiveType -> {
                    Sym boxVar = genSym.generate("box");
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
                                    boxVar,
                                    new VarExpr(fromVar),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    toType,
                                    toVar,
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
                                    toVar,
                                    new VarExpr(fromVar),
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
            Sym envVar) {
        String name = tableRef.getName();
        List<Ident> idents = tableRef.getIdents();
        try {
            switch(tableRef.getType()) {
                case INLINE:
                    return translateTable(tables.stream()
                                  .filter(table -> table.getName().equals(name))
                                  .findFirst()
                                  .get(),
                            idents,
                            envVar);
                case CSV:
                    return parseCSV(name, CSVFormat.DEFAULT.withHeader(), idents, envVar);
                case TSV:
                    return parseCSV(name, CSVFormat.TDF.withHeader(), idents, envVar);
                case EXCEL:
                    return parseExcel(name, idents, envVar);
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
            Sym envVar) {
        return table.getRows()
                .stream()
                .map(row ->
                        translateRow(row, table.getHeader(), idents, envVar))
                .collect(Collectors.toList());
    }

    List<Statement> translateRow(
            Row row,
            List<Ident> header,
            List<Ident> idents,
            Sym envVar) {
        return IntStream.range(0, header.size())
                .filter(i -> idents.contains(header.get(i)))
                .mapToObj(Integer::new)
                .flatMap(i -> {
                    Cell cell = row.getCells().get(i);
                    return cell.accept(exprCell -> {
                                Sym var = genSym.generate(header.get(i).getName());
                                return Stream.concat(translateExpr(exprCell.getExpr(), var, Object.class, envVar),
                                        expressionStrategy.bind(envVar, header.get(i), var).stream());
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
            String fileName, CSVFormat format, List<Ident> idents, Sym envVar)
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
                                        Sym var = genSym.generate(ident.getName());
                                        return Stream.concat(expressionStrategy.eval(var,
                                                new yokohama.unit.ast.QuotedExpr(
                                                        record.get(ident.getName()),
                                                        new yokohama.unit.position.Span(
                                                                Optional.of(Paths.get(fileName)),
                                                                new Position((int)parser.getCurrentLineNumber(), -1),
                                                                new Position(-1, -1))),
                                                Object.class, envVar).stream(),
                                                expressionStrategy.bind(envVar, ident, var).stream());
                                    })
                                    .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<List<Statement>> parseExcel(
            String fileName, List<Ident> idents, Sym envVar)
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
                                    Sym var = genSym.generate(names.get(i));
                                    return Stream.concat(expressionStrategy.eval(var,
                                            new yokohama.unit.ast.QuotedExpr(
                                                    row.getCell(left + i).getStringCellValue(),
                                                    new yokohama.unit.position.Span(
                                                            Optional.of(Paths.get(fileName)),
                                                            new Position(row.getRowNum() + 1, left + i + 1),
                                                            new Position(-1, -1))),
                                            Object.class, envVar).stream(),
                                            expressionStrategy.bind(envVar, ident, var).stream());
                                })
                                .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<Method> translateFourPhaseTest(FourPhaseTest fourPhaseTest) {
        Sym env = genSym.generate("env");

        String testName = SUtils.toIdent(fourPhaseTest.getName());
        Stream<Statement> bindings;
        if (fourPhaseTest.getSetup().isPresent()) {
            Phase setup = fourPhaseTest.getSetup().get();
            bindings = setup.getLetStatements().stream()
                    .flatMap(letStatement ->
                            letStatement.getBindings().stream()
                                    .flatMap(binding -> {
                                        Sym var = genSym.generate(binding.getName().getName());
                                        return Stream.concat(translateExpr(binding.getValue(),
                                                        var,
                                                        Object.class,
                                                        env),
                                                expressionStrategy.bind(
                                                        env,
                                                        binding.getName(),
                                                        var)
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
        Stream<Statement> testStatements =
                fourPhaseTest.getVerify().getAssertions().stream()
                        .flatMap(assertion ->
                                translateAssertion(assertion, env)
                                        .stream().flatMap(s -> s.stream()));

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
                Arrays.asList(annotationOf(TEST)),
                testName,
                Arrays.asList(),
                Optional.empty(),
                Arrays.asList(ClassType.EXCEPTION),
                ListUtils.union(
                        expressionStrategy.env(env),
                        actionsAfter.size() > 0
                                ?  Arrays.asList(
                                        new TryStatement(
                                                statements,
                                                Arrays.asList(),
                                                actionsAfter))
                                : statements)));
    }

    Stream<Statement> translateStatements(
            List<yokohama.unit.ast.Statement> statements, Sym envVar) {
        return statements.stream()
                .flatMap(statement -> statement.accept(execution -> translateExecution(execution, envVar),
                        invoke -> translateInvoke(invoke, envVar)));
    }

    Stream<Statement> translateExecution(
            Execution execution, Sym envVar) {
        Sym __ = genSym.generate("__");
        return execution.getExpressions().stream()
                .flatMap(expression ->
                        expressionStrategy.eval(__, expression, Object.class, envVar).stream());
    }

    Stream<Statement> translateInvoke(Invoke invoke, Sym envVar) {
        yokohama.unit.ast.ClassType classType = invoke.getClassType();
        Class<?> clazz = classType.toClass(classResolver);
        MethodPattern methodPattern = invoke.getMethodPattern();
        String methodName = methodPattern.getName();
        List<yokohama.unit.ast.Type> argTypes = methodPattern.getParamTypes();
        boolean isVararg = methodPattern.isVararg();
        Optional<yokohama.unit.ast.Expr> receiver = invoke.getReceiver();
        List<yokohama.unit.ast.Expr> args = invoke.getArgs();

        Pair<List<Sym>, Stream<Statement>> argVarsAndStatements =
                setupArgs(argTypes, isVararg, args, envVar);
        List<Sym> argVars = argVarsAndStatements.getFirst();
        Stream<Statement> setupStatements = argVarsAndStatements.getSecond();

        Optional<Type> returnType = invoke.getMethodPattern()
                .getReturnType(classType, classResolver)
                .map(type -> Type.of(type, classResolver));
        Stream<Statement> invocation;
        if (returnType.isPresent()) {
            invocation = generateInvoke(genSym.generate("__"),
                    classType,
                    methodName,
                    argTypes,
                    isVararg,
                    argVars,
                    receiver,
                    returnType.get(),
                    envVar,
                    invoke.getSpan());
        } else {
            invocation = generateInvokeVoid(classType,
                    methodName,
                    argTypes,
                    isVararg,
                    argVars,
                    receiver,
                    envVar,
                    invoke.getSpan());
        }

        return Stream.concat(setupStatements, invocation);
    }

    Pair<List<Sym>, Stream<Statement>> setupArgs(
            List<yokohama.unit.ast.Type> argTypes,
            boolean isVararg,
            List<yokohama.unit.ast.Expr> args,
            Sym envVar) {
        List<Pair<Sym, Stream<Statement>>> setupArgs;
        if (isVararg) {
            int splitAt = argTypes.size() - 1;
            List<Pair<yokohama.unit.ast.Type, List<yokohama.unit.ast.Expr>>> typesAndArgs = 
                    Pair.zip(
                            argTypes,
                            Lists.split(args, splitAt).map((nonVarargs, varargs) ->
                                    ListUtils.union(
                                            Lists.map(nonVarargs, Arrays::asList),
                                            Arrays.asList(varargs))));
            setupArgs = Lists.mapInitAndLast(typesAndArgs,
                    typeAndArg -> typeAndArg.map((t, arg) -> {
                        Sym argVar = genSym.generate("arg");
                        Type paramType = Type.of(t, classResolver);
                        Stream<Statement> expr = translateExpr(arg.get(0), argVar, paramType.toClass(), envVar);
                        return new Pair<>(argVar, expr);
                    }),
                    typeAndArg -> typeAndArg.map((t, varargs) -> {
                        Type paramType = Type.of(t, classResolver);
                        List<Pair<Sym, Stream<Statement>>> exprs = varargs.stream().map(vararg -> {
                                    Sym varargVar = genSym.generate("vararg");
                                    Stream<Statement> expr = translateExpr(vararg,
                                            varargVar,
                                            paramType.toClass(),
                                            envVar);
                                    return new Pair<>(varargVar, expr);
                                }).collect(Collectors.toList());
                        List<Sym> varargVars = Pair.unzip(exprs).getFirst();
                        Stream<Statement> varargStatements = exprs.stream().flatMap(Pair::getSecond);
                        Sym argVar = genSym.generate("arg");
                        Stream<Statement> arrayStatement = Stream.of(
                                new VarInitStatement(
                                        paramType.toArray(),
                                        argVar,
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
                        Sym argVar = genSym.generate("arg");
                        Type paramType = Type.of(t, classResolver);
                        Stream<Statement> expr = translateExpr(arg, argVar, paramType.toClass(), envVar);
                        return new Pair<>(argVar, expr);
                    })).collect(Collectors.toList());
        }
        List<Sym> argVars = Pair.unzip(setupArgs).getFirst();
        Stream<Statement> setupStatements =
                setupArgs.stream().flatMap(Pair::getSecond);

        return new Pair<>(argVars, setupStatements);
    }

    Stream<Statement> generateInvoke(
            Sym var,
            yokohama.unit.ast.ClassType classType,
            String methodName,
            List<yokohama.unit.ast.Type> argTypes,
            boolean isVararg,
            List<Sym> argVars,
            Optional<yokohama.unit.ast.Expr> receiver,
            Type returnType,
            Sym envVar,
            Span span) {
        Class<?> clazz = classType.toClass(classResolver);

        if (receiver.isPresent()) {
            // invokevirtual
            Sym receiverVar = genSym.generate("receiver");
            Stream<Statement> getReceiver = translateExpr(receiver.get(), receiverVar, clazz, envVar);
            return Stream.concat(getReceiver, Stream.of(new VarInitStatement(
                            returnType,
                            var,
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
            return Stream.of(new VarInitStatement(
                            returnType,
                            var,
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
            List<Sym> argVars,
            Optional<yokohama.unit.ast.Expr> receiver,
            Sym envVar,
            Span span) {
        Class<?> clazz = classType.toClass(classResolver);

        if (receiver.isPresent()) {
            // invokevirtual
            Sym receiverVar = genSym.generate("receiver");
            Stream<Statement> getReceiver = translateExpr(receiver.get(), receiverVar, clazz, envVar);
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
