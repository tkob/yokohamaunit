package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class StubExpr implements Expr {
    private ClassType classToStub;
    private List<StubBehavior> behavior;
    private Span span;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitStubExpr(this);
    }
    
}
