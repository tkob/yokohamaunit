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
public class IsNotPredicate implements Predicate {
    private Matcher complement;
    private Span span;

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visitIsNotPredicate(this);
    }

    @Override
    public String getDescription() {
        return "is not " + complement.getDescription();
    }
    
}
