package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class CharLitExpr implements Expr {
    private final char value;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitCharLitExpr(this);
    }
    
}
