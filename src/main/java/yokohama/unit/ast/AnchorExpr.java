package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class AnchorExpr implements Expr {
    String anchor;
    Span span;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitAnchorExpr(this);
    }
}
