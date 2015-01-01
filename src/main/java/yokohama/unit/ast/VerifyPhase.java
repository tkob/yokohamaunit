package yokohama.unit.ast;

import java.util.List;
import java.util.Optional;
import lombok.Value;

@Value
public class VerifyPhase {
    private int numHashes;
    private Optional<String> description;
    private List<Assertion> assertions; 
}