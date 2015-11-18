package yokohama.unit.ast;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Fixture {

    public abstract <T> T accept(FixtureVisitor<T> visitor);
    
    public <T> T accept(Supplier<T> visitNone_, Function<TableRef, T> visitTableRef_, Function<Bindings, T> visitBindings_) {
        return accept(new FixtureVisitor<T>() {
            @Override
            public T visitNone() {
                return visitNone_.get();
            }
            @Override
            public T visitTableRef(TableRef tableRef) {
                return visitTableRef_.apply(tableRef);
            }
            @Override
            public T visitBindings(Bindings bindings) {
                return visitBindings_.apply(bindings);
            }
        });
    }

    private static final Fixture none_ =
            new Fixture() {
                @Override
                public <T> T accept(FixtureVisitor<T> visitor) {
                    return visitor.visitNone();
                }
            };

    public static Fixture none() {
        return none_;
    }
}