package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Test implements Definition {
    private String name;
    private List<Assertion> assertions;    
    private int numHashes;
    private Span span;

    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitTest(this);
    }
}
