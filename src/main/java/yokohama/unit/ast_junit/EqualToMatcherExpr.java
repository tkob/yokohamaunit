package yokohama.unit.ast_junit;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper = false)
public class EqualToMatcherExpr extends MatcherExpr {
    private Var operand;

    @Override
    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("org.hamcrest.Matcher ", varName, " = org.hamcrest.CoreMatchers.is(", operand.getName(), ");");
    }
}
