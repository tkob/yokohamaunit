package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class NewExpr implements Expr {
    private final String type;
    private final List<Type> argTypes;
    private final List<Var> args;

    public void getExpr(SBuilder sb, String varName) {
        sb.appendln(varName, " = new ", type, "(",
                args.stream().map(Var::getName).collect(Collectors.joining(", ")),
                ");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitNewExpr(this);
    }
}
