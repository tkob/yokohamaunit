package yokohama.unit.ast_junit;

import java.util.function.Function;
import yokohama.unit.util.SBuilder;

public interface Statement {
    void toString(SBuilder sb);

    <T> T accept(StatementVisitor<T> visitor);

    default <T> T accept(
            Function<IsStatement, T> visitIsStatement_,
            Function<IsNotStatement, T> visitIsNotStatement_,
            Function<VarInitStatement, T> visitVarInitStatement_,
            Function<TryStatement, T> visitTryStatement_,
            Function<IfStatement, T> visitIfStatement_
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
            public T visitVarInitStatement(VarInitStatement varInitStatement) {
                return visitVarInitStatement_.apply(varInitStatement);
            }

            @Override
            public T visitTryStatement(TryStatement tryStatement) {
                return visitTryStatement_.apply(tryStatement);
            }

            @Override
            public T visitIfStatement(IfStatement IfStatement) {
                return visitIfStatement_.apply(IfStatement);
            }
        });
    }
}
