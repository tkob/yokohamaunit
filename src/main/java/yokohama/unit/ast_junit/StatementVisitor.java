package yokohama.unit.ast_junit;

public interface StatementVisitor<T> {
    T visitIsStatement(IsStatement isStatement);
    T visitIsNotStatement(IsNotStatement isNotStatement);
    T visitActionStatement(ActionStatement actionStatement);
    T visitTopBindStatement(TopBindStatement topBindStatement);
    T visitVarDeclStatement(VarDeclStatement varDeclStatement);
    T visitBindThrownStatement(BindThrownStatement bindThrownStatement);
}
