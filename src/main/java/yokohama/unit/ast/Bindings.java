package yokohama.unit.ast;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(exclude={"span"}, callSuper=false)
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class Bindings extends Fixture {
    private List<Binding> bindings;
    private Span span;

    @Override
    public <T> T accept(FixtureVisitor<T> visitor) {
        return visitor.visitBindings(this);
    }

}
