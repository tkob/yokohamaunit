package yokohama.unit.ast_junit;

interface TestStatementVisitor<T> {
    T visitIsStatement(IsStatement isStatement);
    T visitIsNotStatement(IsNotStatement isNotStatement);
    T visitThrowsStatement(ThrowsStatement throwsStatement);
}
