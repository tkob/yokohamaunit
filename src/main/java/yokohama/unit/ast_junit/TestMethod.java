package yokohama.unit.ast_junit;

import java.util.List;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class TestMethod {
    private final String name;
    private final List<Statement> before;
    private final List<Statement> statements;
    private final List<ActionStatement> actionsAfter;

    public void toString(
            SBuilder sb,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy
    ) {
        sb.appendln("@org.junit.Test");
        sb.appendln("public void ", name, "() throws Exception {");
        sb.shift();
        before.forEach(statement -> statement.toString(sb, expressionStrategy, mockStrategy));
        sb.appendln(expressionStrategy.environment());
        if (actionsAfter.size() > 0) {
            sb.appendln("try {");
            sb.shift();
        }
        statements.forEach(testStatement -> testStatement.toString(sb, expressionStrategy, mockStrategy));
        if (actionsAfter.size() > 0) {
            sb.unshift();
            sb.appendln("} finally {");
            sb.shift();
            actionsAfter.forEach(actionStatement -> actionStatement.toString(sb, expressionStrategy, mockStrategy));
            sb.unshift();
            sb.appendln("}");
        }
        sb.unshift();
        sb.appendln("}");
    }
}
