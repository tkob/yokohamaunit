package yokohama.unit.ast;

import javaslang.Tuple;
import javaslang.Tuple2;
import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Abbreviation {
    private final String shortName;
    private final String longName;
    private final Span span;

    public Tuple2<String, String> toPair() {
        return Tuple.of(shortName, longName);
    }
}
