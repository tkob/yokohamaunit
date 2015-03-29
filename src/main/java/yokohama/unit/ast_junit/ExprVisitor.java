package yokohama.unit.ast_junit;

public interface ExprVisitor<T> {
    T visitVarExpr(VarExpr varExpr);
    T visitInstanceOfMatcherExpr(InstanceOfMatcherExpr instanceOfMatcherExpr);
    T visitNullValueMatcherExpr(NullValueMatcherExpr nullValueMatcherExpr);
    T visitConjunctionMatcherExpr(ConjunctionMatcherExpr conjunctionMatcherExpr);
    T visitEqualToMatcherExpr(EqualToMatcherExpr equalToMatcherExpr);
    T visitSuchThatMatcherExpr(SuchThatMatcherExpr suchThatMatcherExpr);
    T visitNewExpr(NewExpr newExpr);
    T visitStrLitExpr(StrLitExpr strLitExpr);
    T visitNullExpr(NullExpr nullExpr);
    T visitInvokeExpr(InvokeExpr invokeExpr);
    T visitInvokeStaticExpr(InvokeStaticExpr aThis);
    T visitIntLitExpr(IntLitExpr intLitExpr);
    T visitClassLitExpr(ClassLitExpr classLitExpr);
    T visitEqualOpExpr(EqualOpExpr equalOpExpr);
}
