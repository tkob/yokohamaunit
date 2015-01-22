package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import lombok.experimental.NonFinal;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.union;

@Value
@NonFinal
public class ClassDecl {
    private final String name;
    private final List<TestMethod> testMethods;

    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return testMethods.stream()
                .<Set<ImportedName>>collect(
                        () -> testMethods.size() > 0 ? union(expressionStrategy.auxMethodsImports(),
                                                             mockStrategy.auxMethodsImports())
                                                     : new TreeSet<>(Arrays.asList()),
                        (set, testMethod) -> set.addAll(testMethod.importedNames(expressionStrategy, mockStrategy)),
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
        if (testMethods.size() > 0) {
            expressionStrategy.auxMethods(sb);
            mockStrategy.auxMethods(sb);
        }
        for (TestMethod testMethod : testMethods) {
            testMethod.toString(sb, expressionStrategy, mockStrategy);
        }
        sb.unshift();
        sb.appendln("}");
    }

}
