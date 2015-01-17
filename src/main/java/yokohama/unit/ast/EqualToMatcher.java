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
public class EqualToMatcher implements Matcher {
    private QuotedExpr expr;
    private Span span;

    @Override
    public <T> T accept(MatcherVisitor<T> visitor) {
        return visitor.visitEqualTo(this);
    }
    
}