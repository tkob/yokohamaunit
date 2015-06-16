package yokohama.unit.ast;

import java.util.List;
import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Clause {
    List<Proposition> propositions;
    Span span;
}
