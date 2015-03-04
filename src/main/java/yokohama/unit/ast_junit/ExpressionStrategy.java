package yokohama.unit.ast_junit;

import java.util.Optional;
import yokohama.unit.util.SBuilder;

public interface ExpressionStrategy {
    public void auxMethods(SBuilder sb);
    public void bind(SBuilder sb, String name, Var varExpr);
    public String getValue(QuotedExpr quotedExpr);
    public Optional<String> wrappingException();
    public String wrappedException(String e);
}
