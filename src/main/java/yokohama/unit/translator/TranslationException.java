package yokohama.unit.translator;

public class TranslationException extends RuntimeException {

    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(Throwable e) {
        super(e);
    }

}
