package yokohama.unit.translator;

import lombok.Getter;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;

@Getter
public class TranslationException extends RuntimeException {
    private final Span span;

    public TranslationException(String message, Span span) {
        super(message);
        this.span = span;
    }

    public TranslationException(Span span, Throwable cause) {
        super(cause);
        this.span = span;
    }

    public TranslationException(String message, Span span, Throwable cause) {
        super(message, cause);
        this.span = span;
    }

    public ErrorMessage toErrorMessage() {
        return new ErrorMessage(this.getMessage(), this.span);
    }
}
