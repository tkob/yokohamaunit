package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class InvokeExpr implements Expr {
    Var object;
    String methodName;
    List<Var> args;

    public void getExpr(SBuilder sb, String varName) {
        sb.appendln(varName, " = ", object.getName(), ".", methodName, "(", args.stream().map(Var::getName).collect(Collectors.joining(", ")), ");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitInvokeExpr(this);
    }
    
}
