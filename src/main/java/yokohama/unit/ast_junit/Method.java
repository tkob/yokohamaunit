package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.Pair;
import yokohama.unit.util.SBuilder;

@Value
public class Method {
    private final List<Annotation> annotations;
    private final String name;
    private final List<Pair<Type, String>> args;
    private final List<Statement> statements;

    public void toString(SBuilder sb) {
        for (Annotation annotation : annotations) {
            annotation.toString(sb);
        }
        sb.appendln(
                "public void ", name, "(",
                args.stream()
                        .map(pair -> pair.getFirst().getText() + " " + pair.getSecond())
                        .collect(Collectors.joining(", ")),
                ") throws Exception {");
        sb.shift();
        for (Pair<Type, String> pair : VarDeclVisitor.sortedSet(new VarDeclVisitor().visitMethod(this))) {
            Type type = pair.getFirst();
            String name = pair.getSecond();
            sb.appendln(type.getText(), " ", name, ";");
        }
        statements.forEach(testStatement -> testStatement.toString(sb));
        sb.unshift();
        sb.appendln("}");
    }
}
