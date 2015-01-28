package yokohama.unit.ast_junit;

import java.util.Set;
import yokohama.unit.util.SBuilder;

abstract public class MatcherExpr implements Expr {
    abstract public void getExpr(SBuilder sb, String varName);
    abstract public Set<ImportedName> importedNames();

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitMatcherExpr(this);
    }
}
