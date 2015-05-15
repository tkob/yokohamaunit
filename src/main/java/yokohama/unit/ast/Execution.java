package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Execution implements Statement {
    private List<QuotedExpr> expressions;
    private Span span;

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitExecution(this);
    }
}
