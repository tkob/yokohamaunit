package yokohama.unit.ast_junit;

import java.util.Optional;
import yokohama.unit.util.SBuilder;

public interface ExpressionStrategy {
    public void auxMethods(SBuilder sb);
    public String getValue(QuotedExpr quotedExpr);
    public Optional<String> wrappingException();
    public String wrappedException(String e);
}
