package yokohama.unit.ast;

public interface ExprVisitor<T> {
    T visitQuotedExpr(QuotedExpr quotedExpr);
    T visitStubExpr(StubExpr stubExpr);
}