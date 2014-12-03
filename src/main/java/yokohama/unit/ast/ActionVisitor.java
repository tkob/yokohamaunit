package yokohama.unit.ast;

public interface ActionVisitor<T> {
    T visitLetBindings(LetBindings letBindings);
    T visitExecution(Execution execution);
}
