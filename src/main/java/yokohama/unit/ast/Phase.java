package yokohama.unit.ast;

import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Phase {
    private int numHashes;
    private Optional<String> description;
    private Optional<LetBindings> letBindings;
    private List<Execution> executions;
    private Span span;
}
