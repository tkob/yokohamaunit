package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Proposition {
    private Expr subject;
    private Predicate predicate;
    private Span span;
}
