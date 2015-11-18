package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class VerifyPhase {
    private Optional<String> description;
    private List<Assertion> assertions; 
    private Span span;
}
