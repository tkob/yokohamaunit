package yokohama.unit.ast;

import lombok.Getter;

@Getter
public class AstException extends RuntimeException {
    private final Span span;
    
    public AstException(String message, Span span) {
        super(message);
        this.span = span;
    }

    public AstException(Span span, Throwable cause) {
        super(cause);
        this.span = span;
    }

    public AstException(String message, Span span, Throwable cause) {
        super(message, cause);
        this.span = span;
    }

    public ErrorMessage toErrorMessage() {
        return new ErrorMessage(this.getMessage(), this.span);
    }
}
