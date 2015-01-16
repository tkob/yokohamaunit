package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class TopBindStatement implements Statement {
    private final String name;
    private final VarExpr value;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        expressionStrategy.bind(sb, name, value);
    }

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return expressionStrategy.bindImports();
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitTopBindStatement(this);
    }
}
