package yokohama.unit.ast_junit;

import java.util.function.Function;
import yokohama.unit.util.SBuilder;

public interface Statement {
    void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy);

    <T> T accept(StatementVisitor<T> visitor);

    default <T> T accept(
            Function<IsStatement, T> visitIsStatement_,
            Function<IsNotStatement, T> visitIsNotStatement_,
            Function<VarInitStatement, T> visitVarInitStatement_,
            Function<ReturnIsStatement, T> visitReturnIsStatement_,
            Function<ReturnIsNotStatement, T> visitReturnIsNotStatement_,
            Function<InvokeVoidStatement, T> visitInvokeVoidStatement_,
            Function<TryStatement, T> visitTryStatement_
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
            public T visitReturnIsStatement(ReturnIsStatement returnIsStatement) {
                return visitReturnIsStatement_.apply(returnIsStatement);
            }

            @Override
            public T visitReturnIsNotStatement(ReturnIsNotStatement returnIsNotStatement) {
                return visitReturnIsNotStatement_.apply(returnIsNotStatement);
            }
            @Override
            public T visitInvokeVoidStatement(InvokeVoidStatement invokeVoidStatement) {
                return visitInvokeVoidStatement_.apply(invokeVoidStatement);
            }

            @Override
            public T visitTryStatement(TryStatement tryStatement) {
                return visitTryStatement_.apply(tryStatement);
            }
        });
    }
}
