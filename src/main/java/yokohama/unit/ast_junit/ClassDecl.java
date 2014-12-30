package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import lombok.experimental.NonFinal;
import yokohama.unit.util.SBuilder;

@Value
@NonFinal
public class ClassDecl {
    private final String name;
    private final List<TestMethod> testMethods;

    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy) {
        return testMethods.stream()
                .collect(
                        () -> new TreeSet<ImportedName>(),
                        (set, testMethod) -> set.addAll(testMethod.importedNames(expressionStrategy)),
                        (s1, s2) -> s1.addAll(s2)
                );
    }

    public void toString(
            SBuilder sb,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy
    ) {
        sb.appendln("public class ", name, " {");
        sb.shift();
        for (TestMethod testMethod : testMethods) {
            testMethod.toString(sb, expressionStrategy, mockStrategy);
        }
        sb.unshift();
        sb.appendln("}");
    }

}
