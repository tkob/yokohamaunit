package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;
import yokohama.unit.util.Pair;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ChoiceBinding implements Binding {
    Ident name;
    List<Expr> choices;
    Span span;

    public Pair<Ident, List<Expr>> toPair() {
        return Pair.of(name, choices);
    }

    @Override
    public <T> T accept(BindingVisitor<T> visitor) {
        return visitor.visitChoiceBinding(this);
    }
}
