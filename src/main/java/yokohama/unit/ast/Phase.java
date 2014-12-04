package yokohama.unit.ast;

import java.util.List;
import java.util.Optional;
import lombok.Value;

@Value
public class Phase {
    private int numHashes;
    private Optional<String> description;
    private List<Action> actions;
}
