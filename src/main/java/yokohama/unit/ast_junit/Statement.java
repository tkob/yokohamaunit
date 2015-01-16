package yokohama.unit.ast_junit;

import java.util.Set;
import java.util.function.Function;
import yokohama.unit.util.SBuilder;

public interface Statement {
    void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy);

    Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy);

    <T> T accept(StatementVisitor<T> visitor);

    default <T> T accept(
            Function<IsStatement, T> visitIsStatement_,
            Function<IsNotStatement, T> visitIsNotStatement_,
            Function<ActionStatement, T> visitActionStatement_,
            Function<TopBindStatement, T> visitTopBindStatement_,
            Function<VarDeclStatement, T> visitVarDeclStatement_,
            Function<BindThrownStatement, T> visitBindThrownStatement_
    ) {
        return accept(new StatementVisitor<T>() {
            @Override
            public T visitIsStatement(IsStatement isStatement) {
                return visitIsStatement_.apply(isStatement);
            }

            @Override
            public T visitIsNotStatement(IsNotStatement isNotStatement) {
                return visitIsNotStatement_.apply(isNotStatement);
            }

            @Override
            public T visitActionStatement(ActionStatement actionStatement) {
                return visitActionStatement_.apply(actionStatement);
            }

            @Override
            public T visitTopBindStatement(TopBindStatement topBindStatement) {
                return visitTopBindStatement_.apply(topBindStatement);
            }

            @Override
            public T visitVarDeclStatement(VarDeclStatement varDeclStatement) {
                return visitVarDeclStatement_.apply(varDeclStatement);
            }

            @Override
            public T visitBindThrownStatement(BindThrownStatement bindThrownStatement) {
                return visitBindThrownStatement_.apply(bindThrownStatement);
            }
        });
    }
}
