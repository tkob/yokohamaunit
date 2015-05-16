package yokohama.unit.ast;

import java.util.function.Function;

public interface Statement {
    <T> T accept(StatementVisitor<T> visitor);

    default <T> T accept(
            Function<Execution, T> visitExecution_,
            Function<Invoke, T> visitInvoke_
    ) {
        return accept(new StatementVisitor<T>() {
            @Override
            public T visitExecution(Execution execution) {
                return visitExecution_.apply(execution);
            }

            @Override
            public T visitInvoke(Invoke invoke) {
                return visitInvoke_.apply(invoke);
            }
        });
    }
    
}
