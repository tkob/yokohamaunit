package yokohama.unit.ast;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(exclude={"span"})
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class IsPredicate implements Predicate {
    private Matcher complement;
    private Span span;

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visitIsPredicate(this);
    }

    @Override
    public String getDescription() {
        return "is " + complement.getDescription();
    }
}
