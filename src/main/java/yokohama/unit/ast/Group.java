package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Group {
    private List<Abbreviation> abbreviations;
    private List<Definition> definitions;
    private Span span;
}
