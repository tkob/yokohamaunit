package yokohama.unit.ast;

public interface DefinitionVisitor<T> {
    T visitTest(Test test);
    T visitTable(Table table);
}
