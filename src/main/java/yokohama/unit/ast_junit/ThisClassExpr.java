package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class ThisClassExpr implements Expr {
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitThisClassExpr(this);
    }
}
