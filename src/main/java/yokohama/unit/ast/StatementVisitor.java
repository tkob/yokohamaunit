package yokohama.unit.ast;

public interface StatementVisitor<T> {
    T visitExecution(Execution execution);
    T visitInvoke(Invoke invoke);
}
