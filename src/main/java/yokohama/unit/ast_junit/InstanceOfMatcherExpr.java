package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;

@Value
public class InstanceOfMatcherExpr extends MatcherExpr {
    private String className;

    @Override
    public String getExpr() {
        return "instanceOf(" + className + ".class)";
    }

    @Override
    public Set<ImportedName> importedNames() {
        return new TreeSet<>(Arrays.asList(
                new ImportStatic("org.hamcrest.CoreMatchers.instanceOf")));
    }
}
