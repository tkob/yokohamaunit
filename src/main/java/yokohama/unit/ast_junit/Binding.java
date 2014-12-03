package yokohama.unit.ast_junit;

import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
public class Binding {
    private final String name;
    private final String value;

    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln("env.put(\"", escapeJava(name), "\", Ognl.getValue(\"", escapeJava(value), "\", env));");
    }
}
