package yokohama.unit.ast;

import java.util.function.Function;

public interface Expr {
    <T> T accept(ExprVisitor<T> visitor);

    default <T> T accept(
            Function<QuotedExpr, T> visitQuotedExpr_,
            Function<StubExpr, T> visitStubExpr_,
            Function<InvocationExpr, T> visitInvocationExpr_,
            Function<IntegerExpr, T> visitIntegerExpr_
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
            public T visitInvocationExpr(InvocationExpr invocationExpr) {
                return visitInvocationExpr_.apply(invocationExpr);
            }
            @Override
            public T visitIntegerExpr(IntegerExpr integerExpr) {
                return visitIntegerExpr_.apply(integerExpr);
            }
        });
    }
}
