package yokohama.unit.translator;

import fj.data.Java;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Copula;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.Execution;
import yokohama.unit.ast.FourPhaseTest;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.LetBindings;
import yokohama.unit.ast.Phase;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.Test;
import yokohama.unit.ast_junit.ActionStatement;
import yokohama.unit.ast_junit.Binding;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.Expr;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.MethodPattern;
import yokohama.unit.ast_junit.NonArrayType;
import yokohama.unit.ast_junit.PrimitiveType;
import yokohama.unit.ast_junit.QuotedExpr;
import yokohama.unit.ast_junit.StubBehavior;
import yokohama.unit.ast_junit.StubExpr;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.TestStatement;
import yokohama.unit.ast_junit.ThrowsStatement;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.util.SUtils;

public class AstToJUnitAst {
    public CompilationUnit translate(String name, Group group, @NonNull String packageName) {
        List<Definition> definitions = group.getDefinitions();
        final List<Table> tables = extractTables(definitions);
        List<TestMethod> methods =
                definitions.stream()
                           .flatMap(definition -> definition.accept(
                                   test -> translateTest(test, tables).stream(),
                                   fourPhaseTest -> translateFourPhaseTest(fourPhaseTest, tables).stream(),
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
        List<TestStatement> testStatements =
                propositions.stream()
                            .map(this::translateProposition)
                            .collect(Collectors.toList());
        return assertion.getFixture().accept(
                () -> Arrays.asList(new TestMethod(methodName, Arrays.asList(), Arrays.asList(), testStatements, Arrays.asList())),
                tableRef -> {
                    List<List<Binding>> table = translateTableRef(tableRef, tables);
                    return IntStream.range(0, table.size())
                            .mapToObj(Integer::new)
                            .map(i -> new TestMethod(methodName + "_" + (i + 1), table.get(i), Arrays.asList(), testStatements, Arrays.asList()))
                            .collect(Collectors.toList());
                },
                bindings -> Arrays.asList(new TestMethod(
                        methodName,
                        bindings.getBindings()
                                .stream()
                                .map(this::translateBinding)
                                .collect(Collectors.toList()),
                        Arrays.asList(),
                        testStatements,
                        Arrays.asList()
                )));
    }

    TestStatement translateProposition(Proposition proposition) {
        String subject = proposition.getSubject().getText();
        String complement = proposition.getComplement().getText();
        Copula copula = proposition.getCopula();
        switch(copula) {
            case IS:
                return new IsStatement(subject, complement);
            case IS_NOT:
                return new IsNotStatement(subject, complement);
            case THROWS:
                return new ThrowsStatement(subject, complement);
        }
        throw new IllegalArgumentException("'" + Objects.toString(copula) + "' is not a copula.");
    }

    Binding translateBinding(yokohama.unit.ast.Binding binding) {
        String name = binding.getName();
        Expr value = translateExpr(binding.getValue());
        return new Binding(name, value);
    }

    Expr translateExpr(yokohama.unit.ast.Expr expr) {
        return expr.accept(
                quotedExpr -> new QuotedExpr(quotedExpr.getText()),
                stubExpr ->
                        new StubExpr(
                                new QuotedExpr(
                                        stubExpr.getClassToStub().getText()
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
                classType -> new ClassType(classType.getName())
        );
    }

    List<List<Binding>> translateTableRef(TableRef tableRef, List<Table> tables) {
        String name = tableRef.getName();
        switch(tableRef.getType()) {
            case INLINE:
                return translateTable(tables.stream()
                              .filter(table -> table.getName().equals(name))
                              .findFirst()
                              .get()
                );
            case CSV:
                return parseCSV(name, CSVFormat.DEFAULT.withHeader());
            case TSV:
                return parseCSV(name, CSVFormat.TDF.withHeader());
            case EXCEL:
                return parseExcel(name);
        }
        throw new IllegalArgumentException("'" + Objects.toString(tableRef) + "' is not a table reference.");

    }

    List<List<Binding>> translateTable(Table table) {
        return table.getRows()
                    .stream()
                    .map(row -> translateRow(row, table.getHeader()))
                    .collect(Collectors.toList());
    }

    List<Binding> translateRow(Row row, List<String> header) {
        // Since vanilla Java has no zip method...
        fj.data.List<String> names = Java.<String>JUList_List().f(header);
        fj.data.List<yokohama.unit.ast.Expr> cells = Java.<yokohama.unit.ast.Expr>JUList_List().f(row.getExprs());
        fj.data.List<Binding> bindings =
                names.zipWith(cells, (name, expr) -> new Binding(name, translateExpr(expr)));
        return Java.<Binding>List_ArrayList().f(bindings);
    }

    @SneakyThrows(IOException.class)
    List<List<Binding>> parseCSV(String fileName, CSVFormat format) {
        try (   final InputStream in = getClass().getResourceAsStream(fileName);
                final Reader reader = new InputStreamReader(in, "UTF-8");
                final CSVParser parser = new CSVParser(reader, format)) {
            return StreamSupport.stream(parser.spliterator(), false)
                    .map(record ->
                            parser.getHeaderMap().keySet()
                                    .stream()
                                    .map(name -> new Binding(name, new QuotedExpr(record.get(name))))
                                    .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }

    List<List<Binding>> parseExcel(String fileName) {
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
                                .map(i -> new Binding(
                                        names.get(i),
                                        new QuotedExpr(row.getCell(left + i).getStringCellValue())))
                                .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        } catch (InvalidFormatException | IOException e) {
            throw new TranslationException(e);
        }
    }

    List<TestMethod> translateFourPhaseTest(FourPhaseTest fourPhaseTest, List<Table> tables) {
        String testName = SUtils.toIdent(fourPhaseTest.getName());
        List<Binding> bindings;
        if (fourPhaseTest.getSetup().isPresent()) {
            Phase setup = fourPhaseTest.getSetup().get();
            if (setup.getLetBindings().isPresent()) {
                LetBindings letBindings = setup.getLetBindings().get();
                bindings = letBindings.getBindings()
                        .stream()
                        .map(binding -> new Binding(binding.getName(), translateExpr(binding.getValue())))
                        .collect(Collectors.toList());
            } else {
                bindings = Arrays.asList();
            }
        } else {
            bindings = Arrays.asList();
        }

        Optional<Stream<ActionStatement>> setupActions =
                fourPhaseTest.getSetup()
                        .map(Phase::getExecutions)
                        .map(this::translateExecutions);
        Optional<Stream<ActionStatement>> exerciseActions =
                fourPhaseTest.getExercise()
                        .map(Phase::getExecutions)
                        .map(this::translateExecutions);
        List<ActionStatement> actionsBefore = Stream.concat(
                setupActions.isPresent() ? setupActions.get() : Stream.empty(),
                exerciseActions.isPresent() ? exerciseActions.get() : Stream.empty()
        ).collect(Collectors.toList());

        List<TestStatement> testStatements = fourPhaseTest.getVerify().getAssertions()
                .stream()
                .flatMap(assertion ->
                        assertion.getPropositions()
                                .stream()
                                .map(this::translateProposition)
                )
                .collect(Collectors.toList());

        List<ActionStatement> actionsAfter;
        if (fourPhaseTest.getTeardown().isPresent()) {
            Phase teardown = fourPhaseTest.getTeardown().get();
            actionsAfter = translateExecutions(teardown.getExecutions()).collect(Collectors.toList());
        } else {
            actionsAfter = Arrays.asList();
        }

        return Arrays.asList(new TestMethod(testName, bindings, actionsBefore, testStatements, actionsAfter));
    }

    Stream<ActionStatement> translateExecutions(List<Execution> executions) {
        return executions.stream()
                .flatMap(execution ->
                        execution.getExpressions()
                                .stream()
                                .map(expression -> new ActionStatement(expression.getText())));
    }
}
