package yokohama.unit.ast_junit;

import java.util.Set;
import yokohama.unit.util.SBuilder;

public interface MockStrategy {
    void auxMethods(SBuilder sb);
    void stub(SBuilder sb, String name, StubExpr stubExpr, ExpressionStrategy expressionStrategy);

    Set<ImportedName> auxMethodsImports();
    Set<ImportedName> stubImports(StubExpr stubExpr, ExpressionStrategy expressionStrategy);
}
