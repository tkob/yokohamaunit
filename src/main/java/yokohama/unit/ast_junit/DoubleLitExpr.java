package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class DoubleLitExpr implements Expr {
    private final double value;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitDoubleLitExpt(this);
    }
    
}
