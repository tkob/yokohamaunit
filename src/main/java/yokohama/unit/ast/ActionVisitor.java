package yokohama.unit.ast;

public interface ActionVisitor<T> {
    T visitAssertion(Assertion assertion);
    T visitLetBindings(LetBindings letBindings);
    T visitExecution(Execution execution);
}
