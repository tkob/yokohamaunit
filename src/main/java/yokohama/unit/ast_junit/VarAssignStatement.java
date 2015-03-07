package yokohama.unit.ast_junit;

import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
public class VarAssignStatement implements Statement {
    private final String name;
    private final ClassType cast;
    private final Expr value;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        value.<Void>accept(
                varExpr -> {
                    sb.appendln(name, " = ", "(", cast.getName(), ")", varExpr.getName(), ";");
                    return null;
                },
                quotedExpr -> {
                    sb.appendln(name, " = ", expressionStrategy.getValue(quotedExpr), ";");
                    return null;
                },
                stubExpr -> {
                    // TODO: need fix, this results in variable initialization, not assignment
                    mockStrategy.stub(sb, name, stubExpr, expressionStrategy);
                    return null;
                },
                matcherExpr -> {
                    // TODO: need fix, this results in variable initialization, not assignment
                    matcherExpr.getExpr(sb, name, expressionStrategy, mockStrategy);
                    return null;
                },
                newExpr -> {
                    // TODO: need fix, this results in variable initialization, not assignment
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
                    // TODO: need fix, this results in variable initialization, not assignment
                    invokeExpr.getExpr(sb, name);
                    return null;
                });
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVarAssignStatement(this);
    }
}
