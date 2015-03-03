package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class NewExpr implements Expr {
    private final String type;

    public void getExpr(SBuilder sb, String varName) {
        sb.appendln(type, " ", varName, " = new ", type, "();");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitNewExpr(this);
    }
}
