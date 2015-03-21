package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class ThisExpr implements Expr {
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitThisExpr(this);
    }
}
