package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.setOf;

@Value
@EqualsAndHashCode(callSuper=false)
public class InstanceOfMatcherExpr extends MatcherExpr {
    private String className;

    @Override
    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("Matcher ", varName, " = instanceOf(", className, ".class);");
    }

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return setOf(new ImportStatic("org.hamcrest.CoreMatchers.instanceOf"));
    }
}
