package yokohama.unit.ast;

import java.util.List;
import java.util.Optional;
import lombok.Value;

@Value
public class FourPhaseTest {
    private int numHashes;
    private String name;
    private Optional<String> setupDescription;
    private List<Action> setup;
    private Optional<String> exerciseDescription;
    private List<Action> exercise;
    private Optional<String> verifyDescription;
    private List<Assertion> verify;
    private Optional<String> teardownDescription;
    private List<Action> teardown;
}
