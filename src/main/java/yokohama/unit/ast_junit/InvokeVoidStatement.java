package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.ast_junit.InvokeExpr.Instruction;
import yokohama.unit.position.Span;
import yokohama.unit.util.SBuilder;

@Value
public class InvokeVoidStatement implements Statement {
    Instruction instruction;
    Var object;
    String methodName;
    List<Type> argTypes;
    List<Var> args;
    Span span;

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
