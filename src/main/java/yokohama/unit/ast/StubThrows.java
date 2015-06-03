package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class StubThrows implements StubBehavior {
    MethodPattern methodPattern;
    Expr exception;
    Span span;

    @Override
    public <T> T accept(StubBehaviorVisitor<T> visitor) {
        return visitor.visitStubThrows(this);
    }
}
