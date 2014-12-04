package yokohama.unit.ast;

import java.util.Optional;
import lombok.Value;

@Value
public class FourPhaseTest {
    private int numHashes;
    private String name;
    private Optional<Phase> setup;
    private Optional<Phase> exercise;
    private Optional<Phase> verify;
    private Optional<Phase> teardown;
}
