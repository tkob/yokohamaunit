package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.InstanceOfMatcher;
import yokohama.unit.ast.InstanceSuchThatMatcher;
import yokohama.unit.ast.IsNotPredicate;
import yokohama.unit.ast.IsPredicate;
import yokohama.unit.ast.LetBindings;
import yokohama.unit.ast.Matcher;
import yokohama.unit.ast.MatcherVisitor;
import yokohama.unit.ast.NullValueMatcher;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.Position;
import yokohama.unit.ast.Predicate;
import yokohama.unit.ast.PredicateVisitor;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableExtractVisitor;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.Test;
import yokohama.unit.ast.ThrowsPredicate;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.ConjunctionMatcherExpr;
import yokohama.unit.ast_junit.EqualToMatcherExpr;
import yokohama.unit.ast_junit.InstanceOfMatcherExpr;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.NonArrayType;
import yokohama.unit.ast_junit.NullExpr;
import yokohama.unit.ast_junit.NullValueMatcherExpr;
import yokohama.unit.ast_junit.PrimitiveType;
import yokohama.unit.ast_junit.ReturnIsNotStatement;
import yokohama.unit.ast_junit.ReturnIsStatement;
import yokohama.unit.ast_junit.Span;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.SuchThatMatcherExpr;
import yokohama.unit.ast_junit.TryStatement;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;
import yokohama.unit.util.SUtils;

public class AstToJUnitAst {
    private final Optional<Path> docyPath;
    private final String className;
    private final String packageName;
    ExpressionStrategy expressionStrategy;
    MockStrategy mockStrategy;
    
    AstToJUnitAst(
            Optional<Path> docyPath,
            String className,
            String packageName,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy) {
        this.docyPath = docyPath;
        this.className = className;
        this.packageName = packageName;
        this.expressionStrategy = expressionStrategy;
        this.mockStrategy = mockStrategy;
    }

    TableExtractVisitor tableExtractVisitor = new TableExtractVisitor();

    public CompilationUnit translate(String name, Group group, @NonNull String packageName) {
        List<Definition> definitions = group.getDefinitions();
        final List<Table> tables = tableExtractVisitor.extractTables(group);
        List<TestMethod> methods =
                definitions.stream()
                           .flatMap(definition -> definition.accept(
                                   test -> translateTest(test, tables).stream(),
                                   fourPhaseTest -> translateFourPhaseTest(fourPhaseTest, tables, new GenSym()).stream(),
                                   table -> Stream.empty()))
                           .collect(Collectors.toList());
        ClassDecl classDecl = new ClassDecl(name, methods);
        return new CompilationUnit(packageName, classDecl);
    }

    List<TestMethod> translateTest(Test test, final List<Table> tables) {
        final String name = test.getName();
        List<Assertion> assertions = test.getAssertions();
        List<TestMethod> testMethods = 
                IntStream.range(0, assertions.size())
                        .mapToObj(Integer::new)
                        .flatMap(i -> translateAssertion(assertions.get(i), i + 1, name, tables).stream())
                        .collect(Collectors.toList());
        return testMethods;
    }

    List<TestMethod> translateAssertion(Assertion assertion, int index, String testName, List<Table> tables) {
        String methodName = SUtils.toIdent(testName) + "_" + index;
        List<Proposition> propositions = assertion.getPropositions();
        return assertion.getFixture().accept(
                () -> {
                    GenSym genSym = new GenSym();
                    String env = genSym.generate("env");
                    return Arrays.asList(new TestMethod(
                            methodName,
                            expressionStrategy.env(env),
                            propositions.stream().flatMap(proposition -> translateProposition(proposition, genSym, env)).collect(Collectors.toList()),
                            Arrays.asList()));
                },
                tableRef -> {
                    GenSym genSym = new GenSym();
                    String env = genSym.generate("env");
                    List<List<Statement>> table = translateTableRef(tableRef, tables, genSym, env);
                    return IntStream.range(0, table.size())
                            .mapToObj(Integer::new)
                            .map(i -> {
                                return new TestMethod(
                                        methodName + "_" + (i + 1),
                                        expressionStrategy.env(env),
                                        ListUtils.union(
                                                table.get(i),
                                                propositions
                                                        .stream()
                                                        .flatMap(proposition -> translateProposition(proposition, genSym, env))
                                                        .collect(Collectors.toList())),
                                        Arrays.asList());
                            })
                            .collect(Collectors.toList());
                },
                bindings -> {
                    GenSym genSym = new GenSym();
                    String env = genSym.generate("env");
                    return Arrays.asList(new TestMethod(
                            methodName,
                            expressionStrategy.env(env),
                            Stream.concat(
                                    bindings.getBindings()
                                            .stream()
                                            .flatMap(binding -> translateBinding(binding, genSym, env)),
                                    propositions.stream().flatMap(proposition -> translateProposition(proposition, genSym, env)))
                                    .collect(Collectors.toList()),
                            Arrays.asList()));
                });
    }

    Stream<Statement> translateProposition(Proposition proposition, GenSym genSym, String envVarName) {
        return proposition.getPredicate().<Stream<Statement>>accept(
                isPredicate -> {
                    String actual = genSym.generate("actual");
                    String expected = genSym.generate("expected");
                    return Stream.concat(
                            expressionStrategy.eval(
                                    actual, envVarName, proposition.getSubject(),
                                    genSym, docyPath, className, packageName).stream(),
                            Stream.concat(
                                    translateMatcher(isPredicate.getComplement(), expected, genSym, envVarName),
                                    Stream.of(new IsStatement(new Var(actual), new Var(expected)))));
                },
                isNotPredicate -> {
                    String actual = genSym.generate("actual");
                    String unexpected = genSym.generate("unexpected");
                    return Stream.concat(
                            expressionStrategy.eval(
                                    actual, envVarName, proposition.getSubject(),
                                    genSym, docyPath, className, packageName).stream(),
                            Stream.concat(
                                    translateMatcher(isNotPredicate.getComplement(), unexpected, genSym, envVarName),
                                    Stream.of(new IsNotStatement(new Var(actual), new Var(unexpected)))));
                },
                throwsPredicate -> {
                    String __ = genSym.generate("tmp");
                    String actual = genSym.generate("actual");
                    String expected = genSym.generate("expected");
                    return Stream.concat(
                            bindThrown(
                                    actual,
                                    expressionStrategy.eval(
                                            __, envVarName, proposition.getSubject(),
                                            genSym, docyPath, className, packageName),
                                    genSym,
                                    envVarName),
                            Stream.concat(
                                    translateMatcher(throwsPredicate.getThrowee(), expected, genSym, envVarName),
                                    Stream.of(new IsStatement(new Var(actual), new Var(expected)))));
                }
        );
    }

    Stream<Statement> bindThrown(String actual, List<Statement> statements, GenSym genSym, String envVarName) {
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
                                Arrays.asList(new VarInitStatement(Type.THROWABLE, actual, new NullExpr()))),
                        Arrays.asList(expressionStrategy.catchAndAssignCause(e, actual, genSym),
                                new CatchClause(
                                        new ClassType("java.lang.Throwable", Span.dummySpan()),
                                        new Var(e),
                                        Arrays.asList(new VarInitStatement(Type.THROWABLE, actual, new VarExpr(e))))),
                        Arrays.asList()));
    }

    Stream<Statement> translateMatcher(Matcher matcher, String varName, GenSym genSym, String envVarName) {
        return matcher.accept(new MatcherVisitor<Stream<Statement>>() {
            @Override
            public Stream<Statement> visitEqualTo(EqualToMatcher equalTo) {
                Var objVar = new Var(genSym.generate("obj"));
                return Stream.concat(
                        expressionStrategy.eval(
                                objVar.getName(), envVarName, equalTo.getExpr(),
                                genSym, docyPath, className, packageName).stream(),
                        Stream.of(new VarInitStatement(
                                Type.MATCHER,
                                varName,
                                new EqualToMatcherExpr(objVar))));
            }
            @Override
            public Stream<Statement> visitInstanceOf(InstanceOfMatcher instanceOf) {
                return Stream.of(new VarInitStatement(
                        Type.MATCHER,
                        varName,
                        new InstanceOfMatcherExpr(instanceOf.getClazz().getName())));
            }
            @Override
            public Stream<Statement> visitInstanceSuchThat(InstanceSuchThatMatcher instanceSuchThat) {
                String bindVarName = instanceSuchThat.getVar().getName();
                String instanceOfVarName = genSym.generate("instanceOfMatcher");
                VarInitStatement instanceOfStatement = new VarInitStatement(
                        Type.MATCHER,
                        instanceOfVarName,
                        new InstanceOfMatcherExpr(instanceSuchThat.getClazz().getName()));
                List<Pair<Var, Stream<Statement>>> suchThats =
                        instanceSuchThat.getPropositions().stream().map(proposition -> {
                            Var suchThatVar = new Var(genSym.generate("suchThat"));
                            Var predVar = new Var(genSym.generate("pred"));
                            yokohama.unit.ast.QuotedExpr subject = proposition.getSubject();
                            Predicate pred = proposition.getPredicate();
                            return pred.accept(new PredicateVisitor<Pair<Var, Stream<Statement>>>() {
                                @Override
                                public Pair<Var, Stream<Statement>> visitIsPredicate(IsPredicate isPredicate) {
                                    Var matchesArg = new Var(genSym.generate("arg"));
                                    Stream<Statement> predStatements =
                                            translateMatcher(isPredicate.getComplement(), predVar.getName(), genSym, envVarName);
                                    Stream<Statement> s = Stream.of(new VarInitStatement(
                                                    Type.MATCHER,
                                                    suchThatVar.getName(),
                                                    new SuchThatMatcherExpr(
                                                            new ArrayList<Statement>() {{
                                                                addAll(expressionStrategy.bind(envVarName, bindVarName, matchesArg, genSym));
                                                                addAll(predStatements.collect(Collectors.toList()));
                                                                addAll(expressionStrategy.eval(
                                                                        "actual", envVarName, subject,
                                                                        genSym, docyPath, className, packageName));
                                                                add(new ReturnIsStatement(new Var("actual"), predVar));
                                                            }},
                                                            proposition.getDescription(),
                                                            matchesArg)));
                                    return new Pair<>(suchThatVar, s);
                                }
                                @Override
                                public Pair<Var, Stream<Statement>> visitIsNotPredicate(IsNotPredicate isNotPredicate) {
                                    Var matchesArg = new Var(genSym.generate("arg"));
                                    Stream<Statement> predStatements =
                                            translateMatcher(isNotPredicate.getComplement(), predVar.getName(), genSym, envVarName);
                                    Stream<Statement> s = Stream.of(new VarInitStatement(
                                                    Type.MATCHER,
                                                    suchThatVar.getName(),
                                                    new SuchThatMatcherExpr(
                                                            new ArrayList<Statement>() {{
                                                                addAll(expressionStrategy.bind(envVarName, bindVarName, matchesArg, genSym));
                                                                addAll(predStatements.collect(Collectors.toList()));
                                                                addAll(expressionStrategy.eval(
                                                                        "actual", envVarName, subject,
                                                                        genSym, docyPath, className, packageName));
                                                                add(new ReturnIsNotStatement(new Var("actual"), predVar));
                                                            }},
                                                            proposition.getDescription(),
                                                            matchesArg)));
                                    return new Pair<>(suchThatVar, s);
                                }
                                @Override
                                public Pair<Var, Stream<Statement>> visitThrowsPredicate(ThrowsPredicate throwsPredicate) {
                                    Var matchesArg = new Var(genSym.generate("arg"));
                                    Stream<Statement> predStatements =
                                            translateMatcher(throwsPredicate.getThrowee(), predVar.getName(), genSym, envVarName);
                                    Stream<Statement> s = Stream.of(new VarInitStatement(
                                                    Type.MATCHER,
                                                    suchThatVar.getName(),
                                                    new SuchThatMatcherExpr(
                                                            new ArrayList<Statement>() {{
                                                                addAll(expressionStrategy.bind(envVarName, bindVarName, matchesArg, genSym));
                                                                addAll(predStatements.collect(Collectors.toList()));
                                                                addAll(bindThrown(
                                                                        "actual",
                                                                        expressionStrategy.eval(
                                                                                "__", envVarName, subject,
                                                                                genSym, docyPath, className, packageName),
                                                                        genSym,
                                                                        envVarName)
                                                                        .collect(Collectors.toList()));
                                                                add(new ReturnIsStatement(new Var("actual"), predVar));
                                                            }},
                                                            proposition.getDescription(),
                                                            matchesArg)));
                                    return new Pair<>(suchThatVar, s);
                                }
                            });
                        }).collect(Collectors.toList());
                Stream<Var> suchThatVars = suchThats.stream().map(Pair::getFirst);
                Stream<Statement> suchThatStatements = suchThats.stream().flatMap(Pair::getSecond);
                VarInitStatement allOfStatement = new VarInitStatement(
                        Type.MATCHER,
                        varName,
                        new ConjunctionMatcherExpr(
                                Stream.concat(
                                        Stream.of(new Var(instanceOfVarName)),
                                        suchThatVars
                                ).collect(Collectors.toList())));
                return Stream.concat(
                        Stream.of(instanceOfStatement),
                        Stream.concat(
                                suchThatStatements,
                                Stream.of(allOfStatement)));
            }
            @Override
            public Stream<Statement> visitNullValue(NullValueMatcher nullValue) {
                return Stream.of(new VarInitStatement(Type.MATCHER, varName, new NullValueMatcherExpr()));
            }
        });
    }

    Stream<Statement> translateBinding(yokohama.unit.ast.Binding binding, GenSym genSym, String envVarName) {
        String name = binding.getName().getName();
        String varName = genSym.generate(name);
        return Stream.concat(
                translateExpr(binding.getValue(), varName, genSym, envVarName),
                expressionStrategy.bind(envVarName, name, new Var(varName), genSym).stream());
    }

    Stream<Statement> translateExpr(yokohama.unit.ast.Expr expr, String varName, GenSym genSym, String envVarName) {
        return expr.accept(
                quotedExpr ->
                        expressionStrategy.eval(
                                varName, envVarName, quotedExpr,
                                genSym, docyPath, className, packageName)
                                .stream(),
                stubExpr -> {
                    return mockStrategy.stub(
                            varName,
                            stubExpr.getClassToStub(),
                            stubExpr.getBehavior(),
                            expressionStrategy,
                            envVarName,
                            genSym,
                            docyPath,
                            className,
                            packageName);
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
                        classType.getName(),
                        new Span(
                                docyPath,
                                classType.getSpan().getStart(),
                                classType.getSpan().getEnd()))
        );
    }

    List<List<Statement>> translateTableRef(TableRef tableRef, List<Table> tables, GenSym genSym, String envVarName) {
        String name = tableRef.getName();
        List<String> idents = tableRef.getIdents()
                                      .stream()
                                      .map(Ident::getName)
                                      .collect(Collectors.toList());
        switch(tableRef.getType()) {
            case INLINE:
                return translateTable(
                        tables.stream()
                              .filter(table -> table.getName().equals(name))
                              .findFirst()
                              .get(),
                        idents,
                        genSym,
                        envVarName);
            case CSV:
                return parseCSV(name, CSVFormat.DEFAULT.withHeader(), idents, genSym, envVarName);
            case TSV:
                return parseCSV(name, CSVFormat.TDF.withHeader(), idents, genSym, envVarName);
            case EXCEL:
                return parseExcel(name, idents, genSym, envVarName);
        }
        throw new IllegalArgumentException("'" + Objects.toString(tableRef) + "' is not a table reference.");

    }

    List<List<Statement>> translateTable(Table table, List<String> idents, GenSym genSym, String envVarName) {
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
                                    genSym,
                                    envVarName))
                    .collect(Collectors.toList());
    }

    List<Statement> translateRow(Row row, List<String> header, List<String> idents, GenSym genSym, String envVarName) {
        return IntStream.range(0, header.size())
                .filter(i -> idents.contains(header.get(i)))
                .mapToObj(Integer::new)
                .flatMap(i -> {
                    String varName = genSym.generate(header.get(i));
                    return Stream.concat(
                            translateExpr(row.getExprs().get(i), varName, genSym, envVarName),
                            expressionStrategy.bind(envVarName, header.get(i), new Var(varName), genSym).stream());
                })
                .collect(Collectors.toList());
    }

    @SneakyThrows(IOException.class)
    List<List<Statement>> parseCSV(String fileName, CSVFormat format, List<String> idents, GenSym genSym, String envVarName) {
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
                                        return Stream.concat(
                                                expressionStrategy.eval(
                                                        varName, envVarName,
                                                        new yokohama.unit.ast.QuotedExpr(
                                                                record.get(name),
                                                                new yokohama.unit.ast.Span(
                                                                        new Position((int)parser.getCurrentLineNumber(), -1),
                                                                        new Position(-1, -1))),
                                                        genSym,
                                                        Optional.of(Paths.get(fileName)), 
                                                        className, packageName).stream(),
                                                expressionStrategy.bind(envVarName, name, new Var(varName), genSym).stream());
                                    })
                                    .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<List<Statement>> parseExcel(String fileName, List<String> idents, GenSym genSym, String envVarName) {
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
                                    return Stream.concat(
                                            expressionStrategy.eval(
                                                    varName, envVarName,
                                                    new yokohama.unit.ast.QuotedExpr(
                                                            row.getCell(left + i).getStringCellValue(),
                                                            new yokohama.unit.ast.Span(
                                                                    new Position(row.getRowNum() + 1, left + i + 1),
                                                                    new Position(-1, -1))),
                                                    genSym,
                                                    Optional.of(Paths.get(fileName)), 
                                                    className, packageName).stream(),
                                            expressionStrategy.bind(envVarName, names.get(i), new Var(varName), genSym).stream());
                                })
                                .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        } catch (InvalidFormatException | IOException e) {
            throw new TranslationException(e);
        }
    }

    List<TestMethod> translateFourPhaseTest(FourPhaseTest fourPhaseTest, List<Table> tables, GenSym genSym) {
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
                                    translateExpr(binding.getValue(), varName, genSym, env),
                                    expressionStrategy.bind(env, binding.getName(), new Var(varName), genSym).stream());
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
                        .map(execition -> translateExecutions(execition, genSym, env));
        Optional<Stream<Statement>> exerciseActions =
                fourPhaseTest.getExercise()
                        .map(Phase::getExecutions)
                        .map(execution -> translateExecutions(execution, genSym, env));
        Stream<Statement> testStatements = fourPhaseTest.getVerify().getAssertions()
                .stream()
                .flatMap(assertion ->
                        assertion.getPropositions()
                                .stream()
                                .flatMap(proposition -> translateProposition(proposition, genSym, env))
                );

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
            actionsAfter = translateExecutions(teardown.getExecutions(), genSym, env).collect(Collectors.toList());
        } else {
            actionsAfter = Arrays.asList();
        }

        return Arrays.asList(new TestMethod(
                testName,
                expressionStrategy.env(env),
                statements,
                actionsAfter));
    }

    Stream<Statement> translateExecutions(List<Execution> executions, GenSym genSym, String envVarName) {
        String __ = genSym.generate("__");
        return executions.stream()
                .flatMap(execution ->
                        execution.getExpressions()
                                .stream()
                                .flatMap(expression ->
                                        expressionStrategy.eval(
                                                __,
                                                envVarName,
                                                expression,
                                                genSym,
                                                docyPath,
                                                className,
                                                packageName).stream()));
    }
}
