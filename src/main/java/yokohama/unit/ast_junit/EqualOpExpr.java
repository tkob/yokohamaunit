package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import lombok.Value;

@Value
public class EqualOpExpr implements Expr {
    private final Sym lhs;
    private final Sym rhs;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitEqualOpExpr(this);
    }
    
}
