package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class ClassLitExpr implements Expr {
    private final ClassType classType;
    
    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitClassLitExpr(this);
    }
}
