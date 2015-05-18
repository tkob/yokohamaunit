package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class InvokeExpr implements Expr {
    ClassType classType;
    Sym object;
    String methodName;
    List<Type> argTypes;
    List<Sym> args;
    Type returnType; // erasued return type

    public void getExpr(SBuilder sb, Type varType, String varName) {
        sb.appendln(varName, " = (", varType.getText(), ")", object.getName(), ".", methodName, "(", args.stream().map(Sym::getName).collect(Collectors.joining(", ")), ");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitInvokeExpr(this);
    }
}
