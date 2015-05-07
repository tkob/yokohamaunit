package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class PredCell implements Cell {
    Predicate predicate;
    Span span;

    @Override
    public <T> T accept(CellVisitor<T> visitor) {
        return visitor.visitPredCell(this);
    }
}
