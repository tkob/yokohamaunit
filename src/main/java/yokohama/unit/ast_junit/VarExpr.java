package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.Sym;

@Value
public class VarExpr implements Expr {
    private final Sym var;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitVarExpr(this);
    }
    
}
