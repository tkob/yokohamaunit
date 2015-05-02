package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class BooleanLitExpr implements Expr {
    private final boolean value;

    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitBooleanLitExpr(this);
    }
}
