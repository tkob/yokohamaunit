package yokohama.unit.ast;

public interface FixtureVisitor<T> {
    T visitNone();
    T visitTableRef(TableRef tableRef);
    T visitBindings(Bindings bindings);    
}
