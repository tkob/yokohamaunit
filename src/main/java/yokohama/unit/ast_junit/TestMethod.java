package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class TestMethod {
    private final String name;
    private final List<Binding> bindings;
    private final List<TestStatement> testStatements;

    public Set<ImportedName> importedNames() {
        return testStatements.stream()
                .collect(
                        () -> new TreeSet<ImportedName>(Arrays.asList(
                                new ImportClass("org.junit.Test"),
                                new ImportClass("ognl.OgnlContext")
                        )),
                        (set, testStatement) -> set.addAll(testStatement.importedNames()),
                        (s1, s2) -> s1.addAll(s2)
                );
    }

    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln("@Test");
        sb.appendln("public void ", name, "() throws Exception {");
        sb.shift();
        sb.appendln("OgnlContext env = new OgnlContext();");
        bindings.forEach(binding -> binding.toString(sb, expressionStrategy));
        testStatements.forEach(testStatement -> testStatement.toString(sb, expressionStrategy));
        sb.unshift();
        sb.appendln("}");
    }
}
