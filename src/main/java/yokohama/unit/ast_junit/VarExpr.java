package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class VarExpr implements Expr {
    private final String name;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
