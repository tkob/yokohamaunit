package yokohama.unit.ast_junit;

public interface ExprVisitor<T> {
    T visitQuotedExpr(QuotedExpr quotedExpr);
    T visitStubExpr(StubExpr stubExpr);
    T visitVarExpr(VarExpr varExpr);
    T visitMatcherExpr(MatcherExpr matcherExpr);
}
