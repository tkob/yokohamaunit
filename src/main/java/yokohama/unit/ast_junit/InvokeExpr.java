package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class InvokeExpr implements Expr {
    Instruction instruction;
    Var object;
    String methodName;
    List<Type> argTypes;
    List<Var> args;
    Type returnType; // erasued return type

    public void getExpr(SBuilder sb, Type varType, String varName) {
        sb.appendln(varName, " = (", varType.getText(), ")", object.getName(), ".", methodName, "(", args.stream().map(Var::getName).collect(Collectors.joining(", ")), ");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitInvokeExpr(this);
    }
    
    public enum Instruction {
        VIRTUAL, INTERFACE
    }
}
