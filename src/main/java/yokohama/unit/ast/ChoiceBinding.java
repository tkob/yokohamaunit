package yokohama.unit.ast;

import java.util.List;
import javaslang.Tuple;
import javaslang.Tuple2;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ChoiceBinding implements Binding {
    Ident name;
    List<Expr> choices;
    Span span;

    public Tuple2<Ident, List<Expr>> toPair() {
        return Tuple.of(name, choices);
    }

    @Override
    public <T> T accept(BindingVisitor<T> visitor) {
        return visitor.visitChoiceBinding(this);
    }
}
