package yokohama.unit.ast;

public interface BindingVisitor<T> {
    T visitSingleBinding(SingleBinding singleBinding);
    T visitChoiceBinding(ChoiceBinding choiceBinding);
    T visitTableBinding(TableBinding tableBinding);
}
