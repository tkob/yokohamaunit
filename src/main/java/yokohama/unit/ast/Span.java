package yokohama.unit.ast;

import lombok.Value;

@Value
public class Span {
    private Position start;
    private Position end;

    public static Span dummySpan() {
        return new Span(new Position(-1, -1), new Position(-1, -1));
    }
}
