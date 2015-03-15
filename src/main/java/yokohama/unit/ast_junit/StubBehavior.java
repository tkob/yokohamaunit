package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class StubBehavior {
    private MethodPattern methodPattern;
    private Var toBeReturned;
}
