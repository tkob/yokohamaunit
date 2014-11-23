package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Bindings extends Fixture {
    private List<Binding> bindings;

    @Override
    public <T> T accept(FixtureVisitor<T> visitor) {
        return visitor.visitBindings(this);
    }

}
