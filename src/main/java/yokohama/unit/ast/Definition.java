package yokohama.unit.ast;

import java.util.function.Function;

public interface Definition {

    <T> T accept(DefinitionVisitor<T> visitor);

    default <T> T accept(Function<Test, T> visitTest_, Function<Table, T> visitTable_) {
        return accept(new DefinitionVisitor<T>() {
            @Override
            public T visitTest(Test test) {
                return visitTest_.apply(test);
            }
            @Override
            public T visitTable(Table table) {
                return visitTable_.apply(table);
            }
        });
    }
}
