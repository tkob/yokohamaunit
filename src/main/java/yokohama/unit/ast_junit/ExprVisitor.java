package yokohama.unit.ast_junit;

public interface ExprVisitor<T> {
    T visitQuotedExpr(QuotedExpr quotedExpr);
    T visitStubExpr(StubExpr stubExpr);
    T visitMatcherExpr(MatcherExpr matcherExpr);
    T visitNewExpr(NewExpr newExpr);
}
