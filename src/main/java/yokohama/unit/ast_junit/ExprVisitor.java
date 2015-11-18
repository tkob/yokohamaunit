package yokohama.unit.ast_junit;

public interface ExprVisitor<T> {
    T visitVarExpr(VarExpr varExpr);
    T visitInstanceOfMatcherExpr(InstanceOfMatcherExpr instanceOfMatcherExpr);
    T visitNullValueMatcherExpr(NullValueMatcherExpr nullValueMatcherExpr);
    T visitEqualToMatcherExpr(EqualToMatcherExpr equalToMatcherExpr);
    T visitRegExpMatcherExpr(RegExpMatcherExpr regExpMatcherExpr);
    T visitNewExpr(NewExpr newExpr);
    T visitStrLitExpr(StrLitExpr strLitExpr);
    T visitNullExpr(NullExpr nullExpr);
    T visitInvokeExpr(InvokeExpr invokeExpr);
    T visitInvokeStaticExpr(InvokeStaticExpr invokeStaticExpr);
    T visitFieldStaticExpr(FieldStaticExpr fieldStaticExpr);
    T visitIntLitExpr(IntLitExpr intLitExpr);
    T visitLongLitExpr(LongLitExpr longLitExpr);
    T visitFloatLitExpr(FloatLitExpr floatLitExpr);
    T visitDoubleLitExpt(DoubleLitExpr doubleLitExpr);
    T visitBooleanLitExpr(BooleanLitExpr booleanLitExpr);
    T visitCharLitExpr(CharLitExpr charLitExpr);
    T visitClassLitExpr(ClassLitExpr classLitExpr);
    T visitEqualOpExpr(EqualOpExpr equalOpExpr);
    T visitArrayExpr(ArrayExpr arrayExpr);
}
