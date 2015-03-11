package yokohama.unit.ast_junit;

import java.util.function.Function;

public interface Expr {
    <T> T accept(ExprVisitor<T> visitor);

    default <T> T accept(
            Function<VarExpr, T> visitVarExpr_,
            Function<QuotedExpr, T> visitQuotedExpr_,
            Function<StubExpr, T> visitStubExpr_,
            Function<MatcherExpr, T> visitMatcherExpr_,
            Function<NewExpr, T> visitNewExpr_,
            Function<StrLitExpr, T> visitStrLitExpr_,
            Function<NullExpr, T> visitNullExpr_,
            Function<InvokeExpr, T> visitInvokeExpr_,
            Function<ThisExpr, T> visitThisExpr_
    ) {
        return accept(new ExprVisitor<T>() {
            @Override
            public T visitVarExpr(VarExpr varExpr) {
                return visitVarExpr_.apply(varExpr);
            }
            @Override
            public T visitQuotedExpr(QuotedExpr quotedExpr) {
                return visitQuotedExpr_.apply(quotedExpr);
            }
            @Override
            public T visitStubExpr(StubExpr stubExpr) {
                return visitStubExpr_.apply(stubExpr);
            }
            @Override
            public T visitMatcherExpr(MatcherExpr matcherExpr) {
                return visitMatcherExpr_.apply(matcherExpr);
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
            public T visitThisExpr(ThisExpr thisExpr) {
                return visitThisExpr_.apply(thisExpr);
            }
        });
    }
}
