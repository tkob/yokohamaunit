package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.Pair;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Binding {
    private Ident name;
    private Expr value;
    private Span span;

    public Pair<Ident, Expr> toPair() {
        return new Pair<>(name, value);
    }
}
