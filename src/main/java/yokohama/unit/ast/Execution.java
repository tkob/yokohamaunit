package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Execution implements Action {
    private List<Expr> expressions;

    @Override
    public <T> T accept(ActionVisitor<T> visitor) {
        return visitor.visitExecution(this);
    }
    
}
