package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class StubReturns implements StubBehavior {
    MethodPattern methodPattern;
    Expr toBeReturned;
    Span span;

    @Override
    public <T> T accept(StubBehaviorVisitor<T> visitor) {
        return visitor.visitStubReturns(this);
    }
}
