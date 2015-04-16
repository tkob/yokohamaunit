package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Abbreviation {
    private final String shortName;
    private final String longName;
    private final Span span;
}
