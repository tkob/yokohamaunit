package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Value;
import static yokohama.unit.util.SetUtils.setOf;

@Value
@EqualsAndHashCode(callSuper=false)
public class InstanceOfMatcherExpr extends MatcherExpr {
    private String className;

    @Override
    public String getExpr() {
        return "instanceOf(" + className + ".class)";
    }

    @Override
    public Set<ImportedName> importedNames() {
        return setOf(new ImportStatic("org.hamcrest.CoreMatchers.instanceOf"));
    }
}
