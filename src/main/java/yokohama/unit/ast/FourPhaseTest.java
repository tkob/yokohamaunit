package yokohama.unit.ast;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class FourPhaseTest implements Definition {
    private int numHashes;
    private String name;
    private Optional<Phase> setup;
    private Optional<Phase> exercise;
    private VerifyPhase verify;
    private Optional<Phase> teardown;
    private Span span;

    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitFourPhaseTest(this);
    }
}
