package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class TempFileExpr implements Expr {
    private Span span;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitTempFileExpr(this);
    }
}
