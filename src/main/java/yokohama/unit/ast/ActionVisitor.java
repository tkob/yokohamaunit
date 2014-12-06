package yokohama.unit.ast;

public interface ActionVisitor<T> {
    T visitExecution(Execution execution);
}
