package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.setOf;
import static yokohama.unit.util.SetUtils.union;

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
                    sb.appendln("Matcher ", name, " = ", matcherExpr.getExpr(), ";");
                    return null;
                });
    }

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return value.accept(
                quotedExpr -> expressionStrategy.getValueImports(), 
                stubExpr -> mockStrategy.stubImports(stubExpr, expressionStrategy),
                matcherExpr -> union(
                        matcherExpr.importedNames(),
                        setOf(new ImportClass("org.hamcrest.Matcher"))));
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVarDeclStatement(this);
    }
}
