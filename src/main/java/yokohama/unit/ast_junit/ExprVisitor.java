package yokohama.unit.ast_junit;

public interface ExprVisitor<T> {
    T visitVarExpr(VarExpr varExpr);
    T visitStubExpr(StubExpr stubExpr);
    T visitMatcherExpr(MatcherExpr matcherExpr);
    T visitNewExpr(NewExpr newExpr);
    T visitStrLitExpr(StrLitExpr strLitExpr);
    T visitNullExpr(NullExpr nullExpr);
    T visitInvokeExpr(InvokeExpr invokeExpr);
    T visitThisExpr(ThisExpr thisExpr);
    T visitInvokeStaticExpr(InvokeStaticExpr aThis);
    T visitIntLitExpr(IntLitExpr intLitExpr);
}
