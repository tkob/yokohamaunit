package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class TableBinding implements Binding {
    List<Ident> idents;
    TableType type;
    String name;
    Span span;

    @Override
    public <T> T accept(BindingVisitor<T> visitor) {
        return visitor.visitTableBinding(this);
    }
}
