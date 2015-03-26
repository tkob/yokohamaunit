package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class EqualOpExpr implements Expr {
    private final Var lhs;
    private final Var rhs;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitEqualOpExpr(this);
    }
    
}
