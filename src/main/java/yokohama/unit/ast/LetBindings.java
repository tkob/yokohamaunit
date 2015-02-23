package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class LetBindings {
    private List<LetBinding> bindings;
    private Span span;
}
