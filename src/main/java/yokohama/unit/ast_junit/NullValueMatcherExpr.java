package yokohama.unit.ast_junit;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper=false)
public class NullValueMatcherExpr implements Expr {
    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln(varName, " = org.hamcrest.CoreMatchers.nullValue();");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitNullValueMatcherExpr(this);
    }
}
