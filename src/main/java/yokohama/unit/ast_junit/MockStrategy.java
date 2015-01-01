package yokohama.unit.ast_junit;

import java.util.Set;
import yokohama.unit.util.SBuilder;

public interface MockStrategy {
    void stub(SBuilder sb, String name, StubExpr stubExpr, ExpressionStrategy expressionStrategy);
    Set<ImportedName> stubImports(StubExpr stubExpr, ExpressionStrategy expressionStrategy);
}
