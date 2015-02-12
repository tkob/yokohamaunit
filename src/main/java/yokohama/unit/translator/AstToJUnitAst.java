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
import lombok.AllArgsConstructor;
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
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.Test;
import yokohama.unit.ast.ThrowsPredicate;
import yokohama.unit.ast_junit.ActionStatement;
import yokohama.unit.ast_junit.BindThrownStatement;
import yokohama.unit.ast_junit.TopBindStatement;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.ConjunctionMatcherExpr;
import yokohama.unit.ast_junit.EqualToMatcherExpr;
import yokohama.unit.ast_junit.Expr;
import yokohama.unit.ast_junit.InstanceOfMatcherExpr;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.MethodPattern;
import yokohama.unit.ast_junit.NonArrayType;
import yokohama.unit.ast_junit.NullValueMatcherExpr;
import yokohama.unit.ast_junit.PrimitiveType;
import yokohama.unit.ast_junit.QuotedExpr;
import yokohama.unit.ast_junit.ReturnIsNotStatement;
import yokohama.unit.ast_junit.ReturnIsStatement;
import yokohama.unit.ast_junit.Span;
import yokohama.unit.ast_junit.StubBehavior;
import yokohama.unit.ast_junit.StubExpr;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.SuchThatMatcherExpr;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.VarDeclStatement;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;
import yokohama.unit.util.SUtils;

@AllArgsConstructor
public class AstToJUnitAst {
    private final Optional<Path> docyPath;

    public CompilationUnit translate(String name, Group group, @NonNull String packageName) {
        List<Definition> definitions = group.getDefinitions();
        final List<Table> tables = extractTables(definitions);
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
    
    List<Table> extractTables(List<Definition> definitions) {
        return definitions.stream()
                          .flatMap(definition -> definition.accept(
                                  test -> Stream.empty(),
                                  fourPhaseTest -> Stream.empty(),
                                  table -> Stream.of(table)))
                          .collect(Collectors.toList());
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
                    return Arrays.asList(new TestMethod(
                            methodName,
                            propositions.stream().flatMap(proposition -> translateProposition(proposition, genSym)).collect(Collectors.toList()),
                            Arrays.asList()));
                },
                tableRef -> {
                    GenSym genSym = new GenSym();
                    List<List<Statement>> table = translateTableRef(tableRef, tables, genSym);
                    return IntStream.range(0, table.size())
                            .mapToObj(Integer::new)
                            .map(i -> {
                                return new TestMethod(
                                        methodName + "_" + (i + 1),
                                        ListUtils.union(
                                                table.get(i),
                                                propositions
                                                        .stream()
                                                        .flatMap(proposition -> translateProposition(proposition, genSym))
                                                        .collect(Collectors.toList())),
                                        Arrays.asList());
                            })
                            .collect(Collectors.toList());
                },
                bindings -> {
                    GenSym genSym = new GenSym();
                    return Arrays.asList(new TestMethod(
                            methodName,
                            Stream.concat(
                                    bindings.getBindings()
                                            .stream()
                                            .flatMap(binding -> translateBinding(binding, genSym)),
                                    propositions.stream().flatMap(proposition -> translateProposition(proposition, genSym)))
                                    .collect(Collectors.toList()),
                            Arrays.asList()));
                });
    }

    Stream<Statement> translateProposition(Proposition proposition, GenSym genSym) {
        QuotedExpr subject = new QuotedExpr(
                proposition.getSubject().getText(),
                new Span(
                        docyPath,
                        proposition.getSubject().getSpan().getStart(),
                        proposition.getSubject().getSpan().getEnd()));
        return proposition.getPredicate().<Stream<Statement>>accept(isPredicate -> {
                    String actual = genSym.generate("actual");
                    String expected = genSym.generate("expected");
                    return Stream.concat(
                            Stream.of(new VarDeclStatement(actual, subject)),
                            Stream.concat(
                                    translateMatcher(isPredicate.getComplement(), expected, genSym),
                                    Stream.of(new IsStatement(new Var(actual), new Var(expected)))));
                },
                isNotPredicate -> {
                    String actual = genSym.generate("actual");
                    String unexpected = genSym.generate("unexpected");
                    return Stream.concat(
                            Stream.of(new VarDeclStatement(actual, subject)),
                            Stream.concat(
                                    translateMatcher(isNotPredicate.getComplement(), unexpected, genSym),
                                    Stream.of(new IsNotStatement(new Var(actual), new Var(unexpected)))));
                },
                throwsPredicate -> {
                    String actual = genSym.generate("actual");
                    String expected = genSym.generate("expected");
                    return Stream.concat(
                            Stream.of(new BindThrownStatement(actual, subject)),
                            Stream.concat(
                                    translateMatcher(throwsPredicate.getThrowee(), expected, genSym),
                                    Stream.of(new IsStatement(new Var(actual), new Var(expected)))));
                }
        );
    }

    Stream<Statement> translateMatcher(Matcher matcher, String varName, GenSym genSym) {
        return matcher.accept(new MatcherVisitor<Stream<Statement>>() {
            @Override
            public Stream<Statement> visitEqualTo(EqualToMatcher equalTo) {
                Var objVar = new Var(genSym.generate("obj"));
                return Stream.of(
                        new VarDeclStatement(
                                objVar.getName(),
                                new QuotedExpr(
                                        equalTo.getExpr().getText(),
                                        new Span(
                                                docyPath,
                                                equalTo.getSpan().getStart(),
                                                equalTo.getSpan().getEnd()))),
                        new VarDeclStatement(
                                varName,
                                new EqualToMatcherExpr(objVar)));
            }
            @Override
            public Stream<Statement> visitInstanceOf(InstanceOfMatcher instanceOf) {
                return Stream.of(new VarDeclStatement(
                        varName,
                        new InstanceOfMatcherExpr(instanceOf.getClazz().getName())));
            }
            @Override
           public Stream<Statement> visitInstanceSuchThat(InstanceSuchThatMatcher instanceSuchThat) {
                String bindVarName = instanceSuchThat.getVarName();
                String instanceOfVarName = genSym.generate("instanceOfMatcher");
                VarDeclStatement instanceOfStatement = new VarDeclStatement(
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
                                            translateMatcher(isPredicate.getComplement(), predVar.getName(), genSym);
                                    Stream<Statement> s = Stream.of(
                                            new VarDeclStatement(
                                                    suchThatVar.getName(),
                                                    new SuchThatMatcherExpr(
                                                            new ArrayList<Statement>() {{
                                                                add(new TopBindStatement(bindVarName, matchesArg));
                                                                addAll(predStatements.collect(Collectors.toList()));
                                                                add(new VarDeclStatement(
                                                                        "actual",
                                                                        new QuotedExpr(
                                                                                subject.getText(),
                                                                                new Span(
                                                                                        docyPath,
                                                                                        subject.getSpan().getStart(),
                                                                                        subject.getSpan().getEnd()))));
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
                                            translateMatcher(isNotPredicate.getComplement(), predVar.getName(), genSym);
                                    Stream<Statement> s = Stream.of(
                                            new VarDeclStatement(
                                                    suchThatVar.getName(),
                                                    new SuchThatMatcherExpr(
                                                            new ArrayList<Statement>() {{
                                                                add(new TopBindStatement(bindVarName, matchesArg));
                                                                addAll(predStatements.collect(Collectors.toList()));
                                                                add(new VarDeclStatement(
                                                                        "actual",
                                                                        new QuotedExpr(
                                                                                subject.getText(),
                                                                                new Span(
                                                                                        docyPath,
                                                                                        subject.getSpan().getStart(),
                                                                                        subject.getSpan().getEnd()))));
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
                                            translateMatcher(throwsPredicate.getThrowee(), predVar.getName(), genSym);
                                    Stream<Statement> s = Stream.of(
                                            new VarDeclStatement(
                                                    suchThatVar.getName(),
                                                    new SuchThatMatcherExpr(
                                                            new ArrayList<Statement>() {{
                                                                add(new TopBindStatement(bindVarName, matchesArg));
                                                                addAll(predStatements.collect(Collectors.toList()));
                                                                add(new BindThrownStatement(
                                                                        "actual",
                                                                        new QuotedExpr(
                                                                                subject.getText(),
                                                                                new Span(
                                                                                        docyPath,
                                                                                        subject.getSpan().getStart(),
                                                                                        subject.getSpan().getEnd()))));
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
                VarDeclStatement allOfStatement = new VarDeclStatement(
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
                return Stream.of(new VarDeclStatement(varName, new NullValueMatcherExpr()));
            }
        });
    }

    Stream<Statement> translateBinding(yokohama.unit.ast.Binding binding, GenSym genSym) {
        String name = binding.getName();
        Expr value = translateExpr(binding.getValue());
        String varName = genSym.generate(name);
        return Stream.of(new VarDeclStatement(varName, value),
                new TopBindStatement(name, new Var(varName)));
    }

    Expr translateExpr(yokohama.unit.ast.Expr expr) {
        return expr.accept(
                quotedExpr -> new QuotedExpr(
                        quotedExpr.getText(),
                        new Span(
                                docyPath,
                                quotedExpr.getSpan().getStart(),
                                quotedExpr.getSpan().getEnd())),
                stubExpr ->
                        new StubExpr(
                                new ClassType(
                                        stubExpr.getClassToStub().getName(),
                                        new Span(
                                                docyPath,
                                                stubExpr.getClassToStub().getSpan().getStart(),
                                                stubExpr.getClassToStub().getSpan().getEnd())
                                ),
                                stubExpr.getBehavior()
                                        .stream()
                                        .map(this::translateStubBehavior)
                                        .collect(Collectors.toList())
                        )
        );
    }

    StubBehavior translateStubBehavior(yokohama.unit.ast.StubBehavior stubBehavior) {
        return new StubBehavior(
                translateMethodPattern(stubBehavior.getMethodPattern()),
                translateExpr(stubBehavior.getToBeReturned()));
    }

    MethodPattern translateMethodPattern(yokohama.unit.ast.MethodPattern methodPattern) {
        return new MethodPattern(
                methodPattern.getName(),
                methodPattern.getArgumentTypes().stream()
                                                .map(this::translateType)
                                                .collect(Collectors.toList()),
                methodPattern.isVarArg()
        );
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

    List<List<Statement>> translateTableRef(TableRef tableRef, List<Table> tables, GenSym genSym) {
        String name = tableRef.getName();
        switch(tableRef.getType()) {
            case INLINE:
                return translateTable(
                        tables.stream()
                              .filter(table -> table.getName().equals(name))
                              .findFirst()
                              .get(),
                        tableRef.getIdents()
                                .stream()
                                .map(Ident::getName)
                                .collect(Collectors.toList()),
                        genSym
                );
            case CSV:
                return parseCSV(name, CSVFormat.DEFAULT.withHeader(), genSym);
            case TSV:
                return parseCSV(name, CSVFormat.TDF.withHeader(), genSym);
            case EXCEL:
                return parseExcel(name, genSym);
        }
        throw new IllegalArgumentException("'" + Objects.toString(tableRef) + "' is not a table reference.");

    }

    List<List<Statement>> translateTable(Table table, List<String> idents, GenSym genSym) {
        return table.getRows()
                    .stream()
                    .map(row -> translateRow(row, table.getHeader(), idents, genSym))
                    .collect(Collectors.toList());
    }

    List<Statement> translateRow(Row row, List<String> header, List<String> idents, GenSym genSym) {
        return IntStream.range(0, header.size())
                .filter(i -> idents.contains(header.get(i)))
                .mapToObj(Integer::new)
                .flatMap(i -> {
                    String varName = genSym.generate(header.get(i));
                    return Stream.of(new VarDeclStatement(varName, translateExpr(row.getExprs().get(i))),
                            new TopBindStatement(header.get(i), new Var(varName)));
                })
                .collect(Collectors.toList());
    }

    @SneakyThrows(IOException.class)
    List<List<Statement>> parseCSV(String fileName, CSVFormat format, GenSym genSym) {
        try (   final InputStream in = getClass().getResourceAsStream(fileName);
                final Reader reader = new InputStreamReader(in, "UTF-8");
                final CSVParser parser = new CSVParser(reader, format)) {
            return StreamSupport.stream(parser.spliterator(), false)
                    .map(record ->
                            parser.getHeaderMap().keySet()
                                    .stream()
                                    .flatMap(name -> {
                                        String varName = genSym.generate(name);
                                        return Stream.of(new VarDeclStatement(
                                                        varName,
                                                        new QuotedExpr(
                                                                record.get(name),
                                                                new Span(
                                                                        Optional.of(Paths.get(fileName)), 
                                                                        new Position((int)parser.getCurrentLineNumber(), -1),
                                                                        new Position(-1, -1)))),
                                                new TopBindStatement(name, new Var(varName)));
                                    })
                                    .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<List<Statement>> parseExcel(String fileName, GenSym genSym) {
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
                                .mapToObj(Integer::new)
                                .flatMap(i -> {
                                    String varName = genSym.generate(names.get(i));
                                    return Stream.of(new VarDeclStatement(
                                                    varName,
                                                    new QuotedExpr(
                                                            row.getCell(left + i).getStringCellValue(),
                                                            new Span(
                                                                    Optional.of(Paths.get(fileName)), 
                                                                    new Position(row.getRowNum() + 1, left + i + 1),
                                                                    new Position(-1, -1)))),
                                            new TopBindStatement(names.get(i), new Var(varName)));
                                })
                                .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        } catch (InvalidFormatException | IOException e) {
            throw new TranslationException(e);
        }
    }

    List<TestMethod> translateFourPhaseTest(FourPhaseTest fourPhaseTest, List<Table> tables, GenSym genSym) {
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
                            return Stream.of(new VarDeclStatement(varName, translateExpr(binding.getValue())),
                                    new TopBindStatement(binding.getName(), new Var(varName)));
                        });
            } else {
                bindings = Stream.empty();
            }
        } else {
            bindings = Stream.empty();
        }

        Optional<Stream<ActionStatement>> setupActions =
                fourPhaseTest.getSetup()
                        .map(Phase::getExecutions)
                        .map(this::translateExecutions);
        Optional<Stream<ActionStatement>> exerciseActions =
                fourPhaseTest.getExercise()
                        .map(Phase::getExecutions)
                        .map(this::translateExecutions);
        Stream<Statement> testStatements = fourPhaseTest.getVerify().getAssertions()
                .stream()
                .flatMap(assertion ->
                        assertion.getPropositions()
                                .stream()
                                .flatMap(proposition -> translateProposition(proposition, genSym))
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


        List<ActionStatement> actionsAfter;
        if (fourPhaseTest.getTeardown().isPresent()) {
            Phase teardown = fourPhaseTest.getTeardown().get();
            actionsAfter = translateExecutions(teardown.getExecutions()).collect(Collectors.toList());
        } else {
            actionsAfter = Arrays.asList();
        }

        return Arrays.asList(new TestMethod(testName, statements, actionsAfter));
    }

    Stream<ActionStatement> translateExecutions(List<Execution> executions) {
        return executions.stream()
                .flatMap(execution ->
                        execution.getExpressions()
                                .stream()
                                .map(expression ->
                                        new ActionStatement(
                                                new QuotedExpr(
                                                        expression.getText(),
                                                        new Span(
                                                                docyPath,
                                                                expression.getSpan().getStart(),
                                                                expression.getSpan().getEnd())))));
    }
}
