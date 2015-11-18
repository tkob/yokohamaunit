package yokohama.unit.ast;

import java.util.function.Function;

public interface Expr {
    <T> T accept(ExprVisitor<T> visitor);

    default <T> T accept(
            Function<QuotedExpr, T> visitQuotedExpr_,
            Function<StubExpr, T> visitStubExpr_,
            Function<InvocationExpr, T> visitInvocationExpr_,
            Function<IntegerExpr, T> visitIntegerExpr_,
            Function<FloatingPointExpr, T> visitFloatingPointExpr_,
            Function<BooleanExpr, T> visitBooleanExpr_,
            Function<CharExpr, T> visitCharExpr_,
            Function<StringExpr, T> visitStringExpr_,
            Function<AnchorExpr, T> visitAnchorExpr_,
            Function<AsExpr, T> visitAsExpr_
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
            @Override
            public T visitFloatingPointExpr(FloatingPointExpr floatingPointExpr) {
                return visitFloatingPointExpr_.apply(floatingPointExpr);
            }
            @Override
            public T visitBooleanExpr(BooleanExpr booleanExpr) {
                return visitBooleanExpr_.apply(booleanExpr);
            }
            @Override
            public T visitCharExpr(CharExpr charExpr) {
                return visitCharExpr_.apply(charExpr);
            }
            @Override
            public T visitStringExpr(StringExpr stringExpr) {
                return visitStringExpr_.apply(stringExpr);
            }
            @Override
            public T visitAnchorExpr(AnchorExpr anchorExpr) {
                return visitAnchorExpr_.apply(anchorExpr);
            }
            @Override
            public T visitAsExpr(AsExpr asExpr) {
                return visitAsExpr_.apply(asExpr);
            }
        });
    }
}
