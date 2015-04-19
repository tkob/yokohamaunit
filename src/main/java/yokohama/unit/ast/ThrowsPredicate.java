package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ThrowsPredicate implements Predicate {
    private Matcher throwee;
    private Span span;

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visitThrowsPredicate(this);
    }
}
