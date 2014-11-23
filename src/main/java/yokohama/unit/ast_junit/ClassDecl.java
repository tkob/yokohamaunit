package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ClassDecl implements Stringifiable {
    private final String name;
    private final List<TestMethod> testMethods;

    public Set<ImportedName> importedNames() {
        return testMethods.stream()
                .collect(
                        () -> new TreeSet<ImportedName>(),
                        (set, testMethod) -> set.addAll(testMethod.importedNames()),
                        (s1, s2) -> s1.addAll(s2)
                );
    }

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("public class ", name, " {");
        sb.shift();
        for (TestMethod testMethod : testMethods) {
            testMethod.toString(sb);
        }
        sb.unshift();
        sb.appendln("}");
    }

}
