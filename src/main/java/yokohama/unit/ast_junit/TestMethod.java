package yokohama.unit.ast_junit;

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
    private final List<ActionStatement> actionsBefore;
    private final List<TestStatement> testStatements;
    private final List<ActionStatement> actionsAfter;

    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy) {
        Set<ImportedName> importedNames = new TreeSet<>();
        importedNames.add(new ImportClass("org.junit.Test"));
        importedNames.addAll(expressionStrategy.environmentImports());
        importedNames.addAll(
                bindings.stream()
                        .flatMap(binding ->
                                expressionStrategy.bindImports().stream())
                        .collect(Collectors.toSet())
        );
        importedNames.addAll(
                actionsBefore
                        .stream()
                        .flatMap(testStatement ->
                                testStatement.importedNames(expressionStrategy).stream())
                        .collect(Collectors.toSet())
        );
        importedNames.addAll(
                testStatements
                        .stream()
                        .flatMap(testStatement ->
                                testStatement.importedNames(expressionStrategy).stream())
                        .collect(Collectors.toSet())
        );
        importedNames.addAll(
                actionsAfter
                        .stream()
                        .flatMap(testStatement ->
                                testStatement.importedNames(expressionStrategy).stream())
                        .collect(Collectors.toSet())
        );
        return importedNames;
    }

    public void toString(
            SBuilder sb,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy
    ) {
        sb.appendln("@Test");
        sb.appendln("public void ", name, "() throws Exception {");
        sb.shift();
        sb.appendln(expressionStrategy.environment());
        if (actionsAfter.size() > 0) {
            sb.appendln("try {");
            sb.shift();
        }
        bindings.forEach(binding -> expressionStrategy.bind(sb, binding, mockStrategy));
        actionsBefore.forEach(actionStatement -> actionStatement.toString(sb, expressionStrategy));
        testStatements.forEach(testStatement -> testStatement.toString(sb, expressionStrategy));
        if (actionsAfter.size() > 0) {
            sb.unshift();
            sb.appendln("} finally {");
            sb.shift();
            actionsAfter.forEach(actionStatement -> actionStatement.toString(sb, expressionStrategy));
            sb.unshift();
            sb.appendln("}");
        }
        sb.unshift();
        sb.appendln("}");
    }
}
