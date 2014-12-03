package yokohama.unit.ast;

import java.util.function.Function;

public interface Action {
    <T> T accept(ActionVisitor<T> visitor);

    default <T> T accept(
            Function<LetBindings, T> visitLetBindings_,
            Function<Execution, T> visitExecution_
    ) {
        return accept(new ActionVisitor<T>() {
            @Override
            public T visitLetBindings(LetBindings letBindings) {
                return visitLetBindings_.apply(letBindings);
            }
            @Override
            public T visitExecution(Execution execution) {
                return visitExecution_.apply(execution);
            }
        });
    }
    
}
