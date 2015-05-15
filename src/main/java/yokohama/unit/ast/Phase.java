package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Phase {
    private Optional<String> description;
    private List<LetStatement> letStatements;
    private List<Statement> statements;
    private Span span;
}
