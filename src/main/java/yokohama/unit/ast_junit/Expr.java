package yokohama.unit.ast_junit;

import java.util.function.Function;

public interface Expr {
    <T> T accept(ExprVisitor<T> visitor);

    default <T> T accept(
            Function<VarExpr, T> visitVarExpr_,
            Function<InstanceOfMatcherExpr, T> visitInstanceOfMatcherExpr_,
            Function<NullValueMatcherExpr, T> visitNullValueMatcherExpr_,
            Function<ConjunctionMatcherExpr, T> visitConjunctionMatcherExpr_,
            Function<EqualToMatcherExpr, T> visitEqualToMatcherExpr_,
            Function<NewExpr, T> visitNewExpr_,
            Function<StrLitExpr, T> visitStrLitExpr_,
            Function<NullExpr, T> visitNullExpr_,
            Function<InvokeExpr, T> visitInvokeExpr_,
            Function<InvokeStaticExpr, T> visitInvokeStaticExpr_,
            Function<IntLitExpr, T> visitIntLitExpr_,
            Function<ClassLitExpr, T> visitClassLitExpr_,
            Function<EqualOpExpr, T> visitEqualOpExpr_
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
            public T visitConjunctionMatcherExpr(ConjunctionMatcherExpr conjunctionMatcherExpr) {
                return visitConjunctionMatcherExpr_.apply(conjunctionMatcherExpr);
            }
            @Override
            public T visitEqualToMatcherExpr(EqualToMatcherExpr equalToMatcherExpr) {
                return visitEqualToMatcherExpr_.apply(equalToMatcherExpr);
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
            public T visitIntLitExpr(IntLitExpr intLitExpr) {
                return visitIntLitExpr_.apply(intLitExpr);
            }
            @Override
            public T visitClassLitExpr(ClassLitExpr classLitExpr) {
                return visitClassLitExpr_.apply(classLitExpr);
            }
            @Override
            public T visitEqualOpExpr(EqualOpExpr equalOpExpr) {
                return visitEqualOpExpr_.apply(equalOpExpr);
            }
        });
    }
}
