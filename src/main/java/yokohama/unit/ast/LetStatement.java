package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class LetStatement {
    private List<Binding> bindings;
    private Span span;
}
