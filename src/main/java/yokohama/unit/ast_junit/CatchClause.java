package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import java.util.List;
import lombok.Value;

@Value
public class CatchClause {
    private final ClassType classType;
    private final Sym var;
    private final List<Statement> statements;
}
