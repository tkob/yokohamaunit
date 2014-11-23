package yokohama.unit.translator;

import fj.data.Java;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Copula;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.Expr;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableRef;
import yokohama.unit.ast.Test;
import yokohama.unit.ast_junit.Binding;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.TestStatement;
import yokohama.unit.ast_junit.ThrowsStatement;
import yokohama.unit.util.SUtils;

public class AstToJUnitAst {
    public CompilationUnit translate(String name, Group group, @NonNull String packageName) {
        List<Definition> definitions = group.getDefinitions();
        final List<Table> tables = extractTables(definitions);
        List<TestMethod> methods =
                extractTests(definitions)
                        .stream()
                        .flatMap(test -> translateTest(test, tables).stream())
                        .collect(Collectors.toList());
        ClassDecl classDecl = new ClassDecl(name, methods);
        return new CompilationUnit(packageName, classDecl);
    }
    
    List<Table> extractTables(List<Definition> definitions) {
        return definitions.stream()
                          .flatMap(definition -> definition.accept(test -> Stream.empty(), table -> Stream.of(table)))
                          .collect(Collectors.toList());
    }

    List<Test> extractTests(List<Definition> definitions) {
        return definitions.stream()
                          .flatMap(definition -> definition.accept(test -> Stream.of(test), table -> Stream.empty()))
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
                () -> Arrays.asList(new TestMethod(methodName, Arrays.asList(), testStatements)),
                tableRef -> {
                    List<List<Binding>> table = translateTableRef(tableRef, tables);
                    return IntStream.range(0, table.size())
                            .mapToObj(Integer::new)
                            .map(i -> new TestMethod(methodName + "_" + (i + 1), table.get(i), testStatements))
                            .collect(Collectors.toList());
                },
                bindings -> Arrays.asList(new TestMethod(
                        methodName,
                        bindings.getBindings()
                                .stream()
                                .map(this::translateBinding)
                                .collect(Collectors.toList()),
                        testStatements
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
        return new Binding(binding.getName(), binding.getValue().getText());
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
                throw new UnsupportedOperationException();
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
        fj.data.List<Expr> cells = Java.<Expr>JUList_List().f(row.getExprs());
        fj.data.List<Binding> bindings =
                names.zipWith(cells, (name, expr) -> new Binding(name, expr.getText()));
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
                                    .map(name -> new Binding(name, record.get(name)))
                                    .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }
}
