package yokohama.unit.ast;

import lombok.Value;

@Value
public class StubBehavior {
    private MethodPattern methodPattern;
    private Expr toBeReturned;
}
