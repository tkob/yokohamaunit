package yokohama.unit.ast_junit;

import java.util.function.Function;

public interface Expr {
    <T> T accept(ExprVisitor<T> visitor);

    default <T> T accept(
            Function<QuotedExpr, T> visitQuotedExpr_,
            Function<StubExpr, T> visitStubExpr_,
            Function<MatcherExpr, T> visitMatcherExpr_
    ) {
        return accept(new ExprVisitor<T>() {
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
        });
    }
}
