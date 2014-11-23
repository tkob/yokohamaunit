package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Test implements Definition {
    private String name;
    private List<Assertion> assertions;    
    private int numHashes;

    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitTest(this);
    }
}
