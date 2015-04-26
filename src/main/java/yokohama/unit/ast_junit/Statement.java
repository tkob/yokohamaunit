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
            Function<IfStatement, T> visitIfStatement_,
            Function<ReturnStatement, T> visitReturnStatement_,
            Function<InvokeVoidStatement, T> visitInvokeVoidStatement_,
            Function<InvokeStaticVoidStatement, T> visitInvokeStaticVoidStatement_
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

            @Override
            public T visitReturnStatement(ReturnStatement returnStatement) {
                return visitReturnStatement_.apply(returnStatement);
            }

            @Override
            public T visitInvokeVoidStatement(InvokeVoidStatement invokeVoidStatement) {
                return visitInvokeVoidStatement_.apply(invokeVoidStatement);
            }

            @Override
            public T visitInvokeStaticVoidStatement(InvokeStaticVoidStatement invokeStaticVoidStatement) {
                return visitInvokeStaticVoidStatement_.apply(invokeStaticVoidStatement);
            }
        });
    }
}
