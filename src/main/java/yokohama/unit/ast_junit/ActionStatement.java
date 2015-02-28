package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ActionStatement implements Statement {
    private QuotedExpr action;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln(expressionStrategy.getValue(action), ";");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitActionStatement(this);
    }
}
