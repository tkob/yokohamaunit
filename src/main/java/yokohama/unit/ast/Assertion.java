package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Assertion  {
    private List<Proposition> propositions;
    private Fixture fixture;
}
