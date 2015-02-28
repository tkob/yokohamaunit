package yokohama.unit.ast_junit;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper=false)
public class NullValueMatcherExpr extends MatcherExpr {
    @Override
    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("org.hamcrest.Matcher ", varName, " = org.hamcrest.CoreMatchers.nullValue();");
    }
}
