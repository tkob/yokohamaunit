package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class Annotation {
    private final ClassType clazz;

    public static final Annotation TEST = new Annotation(ClassType.TEST);

    public void toString(SBuilder sb) {
        sb.appendln("@", clazz.getText());
    }
}
