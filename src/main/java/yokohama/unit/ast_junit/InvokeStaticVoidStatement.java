package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.position.Span;
import yokohama.unit.util.SBuilder;

@Value
public class InvokeStaticVoidStatement implements Statement {
    ClassType clazz;
    List<Type> typeArgs;
    String methodName;
    List<Type> argTypes;
    List<Var> args;
    Span span;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln(
                clazz.getText(), ".", 
                typeArgs.size() > 0
                        ? "<" + typeArgs.stream().map(Type::getText).collect(Collectors.joining(", "))  + ">"
                        : "",
                methodName, "(", args.stream().map(Var::getName).collect(Collectors.joining(", ")), ");");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitInvokeStaticVoidStatement(this);
    }
}
