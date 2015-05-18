package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper = false)
public class EqualToMatcherExpr implements Expr {
    private Sym operand;

    public void getExpr(SBuilder sb, String varName) {
        sb.appendln(varName, " = org.hamcrest.CoreMatchers.is(", operand.getName(), ");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitEqualToMatcherExpr(this);
    }
}
