package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class NewExpr implements Expr {
    private final String type;
    private final List<Type> argTypes;
    private final List<Sym> args;

    public void getExpr(SBuilder sb, String varName) {
        sb.appendln(varName, " = new ", type, "(",
                args.stream().map(Sym::getName).collect(Collectors.joining(", ")),
                ");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitNewExpr(this);
    }
}
