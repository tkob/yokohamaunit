package yokohama.unit.ast_junit;

public interface ExprVisitor<T> {
    T visitVarExpr(VarExpr varExpr);
    T visitQuotedExpr(QuotedExpr quotedExpr);
    T visitStubExpr(StubExpr stubExpr);
    T visitMatcherExpr(MatcherExpr matcherExpr);
    T visitNewExpr(NewExpr newExpr);
    T visitStrLitExpr(StrLitExpr strLitExpr);
    T visitNullExpr(NullExpr nullExpr);
    T visitInvokeExpr(InvokeExpr invokeExpr);
    T visitThisExpr(ThisExpr thisExpr);
}
