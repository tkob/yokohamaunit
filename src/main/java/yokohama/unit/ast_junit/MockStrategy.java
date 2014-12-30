package yokohama.unit.ast_junit;

import yokohama.unit.util.SBuilder;

public interface MockStrategy {
    void stub(SBuilder sb, String name, StubExpr stubExpr, ExpressionStrategy expressionStrategy);
}
