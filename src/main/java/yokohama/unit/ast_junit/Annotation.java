package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class Annotation {
    private final ClassType clazz;

    public void toString(SBuilder sb) {
        sb.appendln("@", clazz.getText());
    }
}
