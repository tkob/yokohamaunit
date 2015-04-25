package yokohama.unit.position;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ErrorMessage {
    private String message;
    private Span span;

    @Override
    public String toString() {
        return span + ": " + message;
    }
}
