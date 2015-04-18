package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Optional;
import lombok.Value;
import lombok.experimental.NonFinal;
import yokohama.unit.util.SBuilder;

@Value
@NonFinal
public class ClassDecl {
    private final boolean accPublic;
    private final String name;
    private final Optional<ClassType> extended;
    private final List<ClassType> implemented;
    private final List<Method> methods;

    public void toString(SBuilder sb) {
        sb.appendln(accPublic ? "public " : "", "class ", name, " {");
        sb.shift();
        for (Method method : methods) {
            method.toString(sb);
        }
        sb.unshift();
        sb.appendln("}");
    }

}
