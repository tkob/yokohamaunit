package yokohama.unit.ast_junit;

public interface StatementVisitor<T> {
    T visitIsStatement(IsStatement isStatement);
    T visitVarInitStatement(VarInitStatement varInitStatement);
    T visitTryStatement(TryStatement tryStatement);
    T visitThrowStatement(ThrowStatement throwStatement);
    T visitIfStatement(IfStatement IfStatement);
    T visitReturnStatement(ReturnStatement returnStatement);
    T visitInvokeVoidStatement(InvokeVoidStatement invokeVoidStatement);
    T visitInvokeStaticVoidStatement(InvokeStaticVoidStatement invokeStaticVoidStatement);
}
