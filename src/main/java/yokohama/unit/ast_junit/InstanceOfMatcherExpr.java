package yokohama.unit.ast_junit;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper=false)
public class InstanceOfMatcherExpr implements Expr {
    private ClassType clazz;

    public void getExpr(SBuilder sb, String varName) {
        sb.appendln(varName, " = org.hamcrest.CoreMatchers.instanceOf(", clazz.getText(), ".class);");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitInstanceOfMatcherExpr(this);
    }
}
