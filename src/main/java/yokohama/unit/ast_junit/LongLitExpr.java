package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class LongLitExpr implements Expr {
    private final long value;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitLongLitExpr(this);
    }
}
