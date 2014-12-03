package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class LetBindings {
    private List<LetBinding> bindings;
    
}
