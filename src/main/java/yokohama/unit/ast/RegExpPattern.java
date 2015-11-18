package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class RegExpPattern implements Pattern {
    String regexp;
    Span span;

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        return visitor.visitRegExpPattern(this);
    }
}
