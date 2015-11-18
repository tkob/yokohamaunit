package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
        sb.appendln(accPublic ? "public " : "", "class ", name,
                extended.isPresent() ? " extends " + extended.get().getText() : "",
                implemented.isEmpty()
                        ? ""
                        : " implements " + implemented.stream().map(ClassType::getText).collect(Collectors.joining(", ")),
                " {");
        sb.shift();
        for (Method method : methods) {
            method.toString(sb);
        }
        sb.unshift();
        sb.appendln("}");
    }

}
