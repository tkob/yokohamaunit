package yokohama.unit.ast;

import lombok.Value;

@Value
public class Binding {
    private String name;
    private Expr value;
}
