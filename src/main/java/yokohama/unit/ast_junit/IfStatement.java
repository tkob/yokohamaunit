package yokohama.unit.ast_junit;

import java.util.List;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class IfStatement implements Statement {
    private final Var cond;
    private final List<Statement> then;
    private final List<Statement> otherwise;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("if (", cond.getName(), ") {");
        sb.shift();
            for (Statement statement : then) {
                statement.toString(sb, expressionStrategy, mockStrategy);
            }
        sb.unshift();
        sb.appendln("} else {");
        sb.shift();
            for (Statement statement : otherwise) {
                statement.toString(sb, expressionStrategy, mockStrategy);
            }
        sb.unshift();
        sb.appendln("}");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitIfStatement(this);
    }
    
}
