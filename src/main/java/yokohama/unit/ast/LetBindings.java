package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class LetBindings implements Action {
    private List<LetBinding> bindings;

    @Override
    public <T> T accept(ActionVisitor<T> visitor) {
        return visitor.visitLetBindings(this);
    }
    
}
