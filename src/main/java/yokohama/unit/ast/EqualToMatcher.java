package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class EqualToMatcher implements Matcher {
    private Expr expr;
    private Span span;

    @Override
    public <T> T accept(MatcherVisitor<T> visitor) {
        return visitor.visitEqualTo(this);
    }
}
