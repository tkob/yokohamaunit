package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Execution {
    private List<QuotedExpr> expressions;
    private Span span;
}
