package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Row {
    private List<Expr> exprs;
    private Span span;
}
