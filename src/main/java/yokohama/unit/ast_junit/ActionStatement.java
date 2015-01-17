package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ActionStatement implements Statement {
    private QuotedExpr action;

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return expressionStrategy.getValueImports();
    }

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln(expressionStrategy.getValue(action), ";");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitActionStatement(this);
    }
}
