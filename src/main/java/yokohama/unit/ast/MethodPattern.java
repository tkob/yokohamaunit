package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class MethodPattern {
    private String name;
    private List<Type> paramTypes;
    private boolean varArg;
    private Span span;
}
