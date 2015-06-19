package yokohama.unit.ast_junit;

import java.util.function.Function;

public interface Expr {
    <T> T accept(ExprVisitor<T> visitor);

    default <T> T accept(
            Function<VarExpr, T> visitVarExpr_,
            Function<InstanceOfMatcherExpr, T> visitInstanceOfMatcherExpr_,
            Function<NullValueMatcherExpr, T> visitNullValueMatcherExpr_,
            Function<EqualToMatcherExpr, T> visitEqualToMatcherExpr_,
            Function<RegExpMatcherExpr, T> visitRegExpMatcherExpr_,
            Function<NewExpr, T> visitNewExpr_,
            Function<StrLitExpr, T> visitStrLitExpr_,
            Function<NullExpr, T> visitNullExpr_,
            Function<InvokeExpr, T> visitInvokeExpr_,
            Function<InvokeStaticExpr, T> visitInvokeStaticExpr_,
            Function<FieldStaticExpr, T> visitFieldStaticExpr_,
            Function<IntLitExpr, T> visitIntLitExpr_,
            Function<LongLitExpr, T> visitLongLitExpr_,
            Function<FloatLitExpr, T> visitFloatLitExpr_,
            Function<DoubleLitExpr, T> visitDoubleLitExpr_,
            Function<BooleanLitExpr, T> visitBooleanLitExpr_,
            Function<CharLitExpr, T> visitCharLitExpr_,
            Function<ClassLitExpr, T> visitClassLitExpr_,
            Function<EqualOpExpr, T> visitEqualOpExpr_,
            Function<ArrayExpr, T> visitArrayExpr_
    ) {
        return accept(new ExprVisitor<T>() {
            @Override
            public T visitVarExpr(VarExpr varExpr) {
                return visitVarExpr_.apply(varExpr);
            }
            @Override
            public T visitInstanceOfMatcherExpr(InstanceOfMatcherExpr instanceOfMatcherExpr) {
                return visitInstanceOfMatcherExpr_.apply(instanceOfMatcherExpr);
            }
            @Override
            public T visitNullValueMatcherExpr(NullValueMatcherExpr nullValueMatcherExpr) {
                return visitNullValueMatcherExpr_.apply(nullValueMatcherExpr);
            }
            @Override
            public T visitEqualToMatcherExpr(EqualToMatcherExpr equalToMatcherExpr) {
                return visitEqualToMatcherExpr_.apply(equalToMatcherExpr);
            }
            @Override
            public T visitRegExpMatcherExpr(RegExpMatcherExpr regExpMatcherExpr) {
                return visitRegExpMatcherExpr_.apply(regExpMatcherExpr);
            }
            @Override
            public T visitNewExpr(NewExpr newExpr) {
                return visitNewExpr_.apply(newExpr);
            }
            @Override
            public T visitStrLitExpr(StrLitExpr strLitExpr) {
                return visitStrLitExpr_.apply(strLitExpr);
            }
            @Override
            public T visitNullExpr(NullExpr nullExpr) {
                return visitNullExpr_.apply(nullExpr);
            }
            @Override
            public T visitInvokeExpr(InvokeExpr invokeExpr) {
                return visitInvokeExpr_.apply(invokeExpr);
            }
            @Override
            public T visitInvokeStaticExpr(InvokeStaticExpr invokeStaticExpr) {
                return visitInvokeStaticExpr_.apply(invokeStaticExpr);
            }
            @Override
            public T visitFieldStaticExpr(FieldStaticExpr fieldStaticExpr) {
                return visitFieldStaticExpr_.apply(fieldStaticExpr);
            }
            @Override
            public T visitIntLitExpr(IntLitExpr intLitExpr) {
                return visitIntLitExpr_.apply(intLitExpr);
            }
            @Override
            public T visitLongLitExpr(LongLitExpr longLitExpr) {
                return visitLongLitExpr_.apply(longLitExpr);
            }
            @Override
            public T visitFloatLitExpr(FloatLitExpr floatLitExpr) {
                return visitFloatLitExpr_.apply(floatLitExpr);
            }
            @Override
            public T visitDoubleLitExpt(DoubleLitExpr doubleLitExpr) {
                return visitDoubleLitExpr_.apply(doubleLitExpr);
            }
            @Override
            public T visitBooleanLitExpr(BooleanLitExpr booleanLitExpr) {
                return visitBooleanLitExpr_.apply(booleanLitExpr);
            }
            @Override
            public T visitCharLitExpr(CharLitExpr charLitExpr) {
                return visitCharLitExpr_.apply(charLitExpr);
            }
            @Override
            public T visitClassLitExpr(ClassLitExpr classLitExpr) {
                return visitClassLitExpr_.apply(classLitExpr);
            }
            @Override
            public T visitEqualOpExpr(EqualOpExpr equalOpExpr) {
                return visitEqualOpExpr_.apply(equalOpExpr);
            }
            @Override
            public T visitArrayExpr(ArrayExpr arrayExpr) {
                return visitArrayExpr_.apply(arrayExpr);
            }
        });
    }
}
