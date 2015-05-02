package yokohama.unit.ast;

public interface ExprVisitor<T> {
    T visitQuotedExpr(QuotedExpr quotedExpr);
    T visitStubExpr(StubExpr stubExpr);
    T visitInvocationExpr(InvocationExpr invocationExpr);
    T visitIntegerExpr(IntegerExpr integerExpr);
    T visitFloatingPointExpr(FloatingPointExpr floatingPointExpr);
    T visitBooleanExpr(BooleanExpr booleanExpr);
}
