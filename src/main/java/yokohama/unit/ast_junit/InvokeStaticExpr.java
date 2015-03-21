package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class InvokeStaticExpr implements Expr {
    ClassType clazz;
    List<Type> typeArgs;
    String methodName;
    List<Var> args;

    public void getExpr(SBuilder sb, String varName) {
        sb.appendln(varName, " = ", clazz.getName(), ".", 
                typeArgs.size() > 0
                        ? "<" + typeArgs.stream().map(Type::getText).collect(Collectors.joining(", "))  + ">"
                        : "",
                methodName, "(", args.stream().map(Var::getName).collect(Collectors.joining(", ")), ");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitInvokeStaticExpr(this);
    }
    
}
