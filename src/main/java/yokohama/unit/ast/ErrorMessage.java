package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ErrorMessage {
    private String message;
    private Span span;
}
