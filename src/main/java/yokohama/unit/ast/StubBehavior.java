package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class StubBehavior {
    private MethodPattern methodPattern;
    private Expr toBeReturned;
    private Span span;
}
