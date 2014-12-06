package yokohama.unit.ast;

import java.util.Optional;
import lombok.Value;

@Value
public class FourPhaseTest implements Definition {
    private int numHashes;
    private String name;
    private Optional<Phase> setup;
    private Optional<Phase> exercise;
    private VerifyPhase verify;
    private Optional<Phase> teardown;

    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitFourPhaseTest(this);
    }
}
