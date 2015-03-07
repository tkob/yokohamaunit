package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class NullExpr implements Expr {
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitNullExpr(this);
    }
}
