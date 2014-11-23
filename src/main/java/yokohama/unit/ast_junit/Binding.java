package yokohama.unit.ast_junit;

import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
public class Binding implements Stringifiable {
    private final String name;
    private final String value;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("env.put(\"", escapeJava(name), "\", Ognl.getValue(\"", escapeJava(value), "\", env));");
    }
}
