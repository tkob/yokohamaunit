package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ExprCell implements Cell {
    Expr expr;
    Span span;

    @Override
    public <T> T accept(CellVisitor<T> visitor) {
        return visitor.visitExprCell(this);
    }
}
