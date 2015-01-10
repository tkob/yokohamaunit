package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class TopBinding {
    private final String name;
    private final Expr value;
}
