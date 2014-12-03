package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Execution {
    private List<Expr> expressions;
    
}
