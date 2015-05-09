package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class FourPhaseTest implements Definition {
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
