package yokohama.unit.ast_junit;

import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
public class VarInitStatement implements Statement {
    private final ClassType type;
    private final String name;
    private final Expr value;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        value.<Void>accept(
                varExpr -> {
                    sb.appendln(name, " = ", varExpr.getName(), ";");
                    return null;
                },
                quotedExpr -> {
                    sb.appendln(name, " = ", expressionStrategy.getValue(quotedExpr), ";");
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
                    sb.appendln(name, " = \"", escapeJava(strLitExpr.getText()), "\";");
                    return null;
                },
                nullExpr -> {
                    sb.appendln(name, " = null;");
                    return null;
                },
                invokeExpr -> {
                    invokeExpr.getExpr(sb, name);
                    return null;
                },
                thisExpr -> {
                    sb.appendln(name, " = this;");
                    return null;
                });
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVarInitStatement(this);
    }
}
