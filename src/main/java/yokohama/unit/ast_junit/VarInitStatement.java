package yokohama.unit.ast_junit;

import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
public class VarInitStatement implements Statement {
    private final String name;
    private final Expr value;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        value.<Void>accept(
                varExpr -> {
                    sb.appendln("Object ", name, " = ", varExpr.getName(), ";");
                    return null;
                },
                quotedExpr -> {
                    sb.appendln("Object ", name, " = ", expressionStrategy.getValue(quotedExpr), ";");
                    return null;
                },
                stubExpr -> {
                    mockStrategy.stub(sb, name, stubExpr, expressionStrategy);
                    return null;
                },
                matcherExpr -> {
                    matcherExpr.getExpr(sb, name, expressionStrategy, mockStrategy);
                    return null;
                },
                newExpr -> {
                    newExpr.getExpr(sb, name);
                    return null;
                },
                strLitExpr -> {
                    sb.appendln("String ", name, " = \"", escapeJava(strLitExpr.getText()), "\";");
                    return null;
                });
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVarInitStatement(this);
    }
}
