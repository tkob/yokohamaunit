package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class InstanceOfMatcher implements Matcher {
    private ClassType clazz;
    private Span span;

    @Override
    public <T> T accept(MatcherVisitor<T> visitor) {
        return visitor.visitInstanceOf(this);
    }
}
