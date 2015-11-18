package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class StrLitExpr implements Expr {
    private String text;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitStrLitExpr(this);
    }
}
