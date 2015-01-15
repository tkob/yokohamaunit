package yokohama.unit.ast_junit;

interface StatementVisitor<T> {
    T visitIsStatement(IsStatement isStatement);
    T visitIsNotStatement(IsNotStatement isNotStatement);
    T visitThrowsStatement(ThrowsStatement throwsStatement);
    T visitActionStatement(ActionStatement actionStatement);
    T visitTopBindStatement(TopBindStatement topBindStatement);
    T visitVarDeclStatement(VarDeclStatement varDeclStatement);
    T visitBindThrownStatement(BindThrownStatement bindThrownStatement);
}
