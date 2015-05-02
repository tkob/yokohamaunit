package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class BooleanExpr implements Expr {
    private final boolean value;
    private final Span span;

    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitBooleanExpr(this);
    }
    
}
