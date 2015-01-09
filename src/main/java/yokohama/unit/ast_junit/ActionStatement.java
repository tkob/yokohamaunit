package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ActionStatement {
    private QuotedExpr action;

    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy) {
        return expressionStrategy.getValueImports();
    }

    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln(expressionStrategy.getValue(action), ";");
    }
}
