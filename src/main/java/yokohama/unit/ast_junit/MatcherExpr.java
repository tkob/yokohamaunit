package yokohama.unit.ast_junit;

import yokohama.unit.util.SBuilder;

abstract public class MatcherExpr implements Expr {
    abstract public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStraegy);

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitMatcherExpr(this);
    }
}
