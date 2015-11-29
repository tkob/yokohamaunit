package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javaslang.Tuple2;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class Method {
    private final List<Annotation> annotations;
    private final String name;
    private final List<Tuple2<Type, String>> args;
    private final Optional<Type> returnType;
    private final List<ClassType> thrown;
    private final List<Statement> statements;

    public void toString(SBuilder sb) {
        for (Annotation annotation : annotations) {
            annotation.toString(sb);
        }
        sb.appendln(
                "public ",
                returnType.isPresent() ? returnType.get().getText() : "void",
                " ", name, "(",
                args.stream()
                        .map(pair -> pair._1().getText() + " " + pair._2())
                        .collect(Collectors.joining(", ")),
                ")",
                thrown.isEmpty()
                        ? ""
                        : " throws " + thrown.stream().map(ClassType::getText).collect(Collectors.joining(", ")),
                " {");
        sb.shift();
        for (Tuple2<Type, String> pair : VarDeclVisitor.sortedSet(new VarDeclVisitor().visitMethod(this))) {
            Type type = pair._1();
            String name = pair._2();
            sb.appendln(type.getText(), " ", name, ";");
        }
        statements.forEach(testStatement -> testStatement.toString(sb));
        sb.unshift();
        sb.appendln("}");
    }
}
