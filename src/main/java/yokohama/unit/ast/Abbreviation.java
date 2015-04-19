package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.Pair;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Abbreviation {
    private final String shortName;
    private final String longName;
    private final Span span;

    public Pair<String, String> toPair() {
        return new Pair<>(shortName, longName);
    }
}
