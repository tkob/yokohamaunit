package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class DoesNotMatchPredicate implements Predicate {
    Pattern pattern;
    Span span;

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visitDoesNotMatchPredicate(this);
    }
}
