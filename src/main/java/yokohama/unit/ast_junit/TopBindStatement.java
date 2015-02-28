package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class TopBindStatement implements Statement {
    private final String name;
    private final Var value;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        expressionStrategy.bind(sb, name, value);
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitTopBindStatement(this);
    }
}
