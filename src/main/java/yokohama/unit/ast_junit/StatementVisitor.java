package yokohama.unit.ast_junit;

public interface StatementVisitor<T> {
    T visitIsStatement(IsStatement isStatement);
    T visitIsNotStatement(IsNotStatement isNotStatement);
    T visitVarInitStatement(VarInitStatement varInitStatement);
    T visitReturnIsStatement(ReturnIsStatement returnIsStatement);
    T visitReturnIsNotStatement(ReturnIsNotStatement returnIsNotStatement);
    T visitInvokeVoidStatement(InvokeVoidStatement returnInvokeVoidStatement);
    T visitTryStatement(TryStatement tryStatement);
    T visitIfStatement(IfStatement IfStatement);
}
