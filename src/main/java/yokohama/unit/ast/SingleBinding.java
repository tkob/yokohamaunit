package yokohama.unit.ast;

import javaslang.Tuple;
import javaslang.Tuple2;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class SingleBinding implements Binding {
    Ident name;
    Expr value;
    Span span;

    public Tuple2<Ident, Expr> toPair() {
        return Tuple.of(name, value);
    }

    @Override
    public <T> T accept(BindingVisitor<T> visitor) {
        return visitor.visitSingleBinding(this);
    }
}
