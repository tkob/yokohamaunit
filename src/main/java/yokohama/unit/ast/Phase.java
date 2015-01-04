package yokohama.unit.ast;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(exclude={"span"})
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class Phase {
    private int numHashes;
    private Optional<String> description;
    private Optional<LetBindings> letBindings;
    private List<Execution> executions;
    private Span span;
}
