package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class FloatLitExpr implements Expr{
    private final float value;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitFloatLitExpr(this);
    }
    
}
