package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Assertion implements Action {
    private List<Proposition> propositions;
    private Fixture fixture;

    @Override
    public <T> T accept(ActionVisitor<T> visitor) {
        return visitor.visitAssertion(this);
    }
}
