package yokohama.unit.ast_junit;

import java.util.Set;

abstract public class MatcherExpr implements Expr {
    abstract public String getExpr();
    abstract public Set<ImportedName> importedNames();

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitMatcherExpr(this);
    }
}
