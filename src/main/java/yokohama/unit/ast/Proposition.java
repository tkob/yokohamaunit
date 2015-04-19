package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Proposition {
    private QuotedExpr subject;
    private Predicate predicate;
    private Span span;
}
