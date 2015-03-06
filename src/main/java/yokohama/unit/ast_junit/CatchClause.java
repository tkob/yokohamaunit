package yokohama.unit.ast_junit;

import java.util.List;
import lombok.Value;

@Value
public class CatchClause {
    private final ClassType classType;
    private final Var var;
    private final List<Statement> statements;
}
