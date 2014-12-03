package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class TestMethod {
    private final String name;
    private final List<Binding> bindings;
    private final List<TestStatement> testStatements;

    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy) {
        Set<ImportedName> importedNames = new TreeSet<>();
        importedNames.add(new ImportClass("org.junit.Test"));
        importedNames.addAll(expressionStrategy.environmentImports());
        importedNames.addAll(
                bindings.stream()
                        .flatMap(binding ->
                                expressionStrategy.bindImports(binding).stream())
                        .collect(Collectors.toSet())
        );
        importedNames.addAll(
                testStatements
                        .stream()
                        .flatMap(testStatement ->
                                testStatement.importedNames(expressionStrategy).stream())
                        .collect(Collectors.toSet())
        );
        return importedNames;
    }

    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln("@Test");
        sb.appendln("public void ", name, "() throws Exception {");
        sb.shift();
        sb.appendln(expressionStrategy.environment());
        bindings.forEach(binding -> sb.appendln(expressionStrategy.bind(binding)));
        testStatements.forEach(testStatement -> testStatement.toString(sb, expressionStrategy));
        sb.unshift();
        sb.appendln("}");
    }
}
