package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class StubExpr implements Expr {
    private QuotedExpr classToStub;
    private List<StubBehavior> behavior;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitStubExpr(this);
    }
    
}
