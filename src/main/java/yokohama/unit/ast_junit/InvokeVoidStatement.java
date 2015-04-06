package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class InvokeVoidStatement implements Statement {
    private final Var object;
    private final String methodName;
    List<Type> argTypes;
    private final List<Var> args;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln(
                object.getName(),
                ".",
                methodName,
                "(",
                args.stream().map(Var::getName).collect(Collectors.joining(", ")),
                ");");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitInvokeVoidStatement(this);
    }
}
