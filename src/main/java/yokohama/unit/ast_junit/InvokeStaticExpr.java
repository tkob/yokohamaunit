package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class InvokeStaticExpr implements Expr {
    ClassType clazz;
    List<Type> typeArgs;
    String methodName;
    List<Type> argTypes;
    List<Sym> args;
    Type returnType; // erasued return type

    public void getExpr(SBuilder sb, Type varType, String varName) {
        sb.appendln(varName, " = (", varType.getText(), ")",
                clazz.getText(), ".", 
                typeArgs.size() > 0
                        ? "<" + typeArgs.stream().map(Type::getText).collect(Collectors.joining(", "))  + ">"
                        : "",
                methodName, "(", args.stream().map(Sym::getName).collect(Collectors.joining(", ")), ");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitInvokeStaticExpr(this);
    }
    
}
