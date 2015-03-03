package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class VarDeclStatement implements Statement {
    private final String name;
    private final Expr value;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        value.<Void>accept(
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
                });
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVarDeclStatement(this);
    }
}
