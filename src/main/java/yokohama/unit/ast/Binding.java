package yokohama.unit.ast;

import java.util.function.Function;

public interface Binding {
    <T> T accept(BindingVisitor<T> visitor);

    default <T> T accept(
            Function<SingleBinding, T> visitSingleBinding_,
            Function<ChoiceBinding, T> visitChoiceBinding_,
            Function<TableBinding, T> visitTableBinding_) {
        return accept(new BindingVisitor<T>() {
            @Override
            public T visitSingleBinding(SingleBinding singleBinding) {
                return visitSingleBinding_.apply(singleBinding);
            }
            @Override
            public T visitChoiceBinding(ChoiceBinding choiceBinding) {
                return visitChoiceBinding_.apply(choiceBinding);
            }
            @Override
            public T visitTableBinding(TableBinding tableBinding) {
                return visitTableBinding_.apply(tableBinding);
            }
        });
    }
}
