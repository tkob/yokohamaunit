package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"}, callSuper=false)
public class Bindings extends Fixture {
    private List<Binding> bindings;
    private Span span;

    @Override
    public <T> T accept(FixtureVisitor<T> visitor) {
        return visitor.visitBindings(this);
    }

}
