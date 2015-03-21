package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class IntLitExpr implements Expr {
    private final int value;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitIntLitExpr(this);
    }
    
}
