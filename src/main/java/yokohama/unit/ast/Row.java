package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Row {
    private List<Expr> exprs;
}
