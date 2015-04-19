package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class IsPredicate implements Predicate {
    private Matcher complement;
    private Span span;

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visitIsPredicate(this);
    }
}
