package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;
import yokohama.unit.util.Pair;

@Value
@EqualsAndHashCode(exclude={"span"})
public class SingleBinding implements Binding {
    Ident name;
    Expr value;
    Span span;

    public Pair<Ident, Expr> toPair() {
        return Pair.of(name, value);
    }

    @Override
    public <T> T accept(BindingVisitor<T> visitor) {
        return visitor.visitSingleBinding(this);
    }
}
