package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class InvocationExpr implements Expr {
    private final List<String> receiver;
    private final MethodPattern methodPattern;
    private final List<Expr> args;
    private final Span span;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitInvocationExpr(this);
    }
    
}
