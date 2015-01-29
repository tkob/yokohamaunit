package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.setOf;

@Value
@EqualsAndHashCode(callSuper=false)
public class NullValueMatcherExpr extends MatcherExpr {
    @Override
    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy) {
        sb.appendln("Matcher ", varName, " = nullValue();");
    }

    @Override
    public Set<ImportedName> importedNames() {
        return setOf(new ImportStatic("org.hamcrest.CoreMatchers.nullValue"));
    }
}
