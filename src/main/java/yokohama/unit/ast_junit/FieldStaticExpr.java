package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class FieldStaticExpr implements Expr {
    ClassType clazz;
    Type fieldType;
    String fieldName;

    public void getExpr(SBuilder sb, Type varType, String varName) {
        sb.appendln(varName, " = (", varType.getText(), ")",
                clazz.getText(), ".", fieldName, ";");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitFieldStaticExpr(this);
    }
}
