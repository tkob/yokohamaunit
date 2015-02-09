package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.union;

@Value
public class BindThrownStatement implements Statement {
    private final String name;
    private final Expr value;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("Throwable ", name, ";");
        sb.appendln("try {");
        sb.shift();
            value.<Void>accept(
                    quotedExpr -> {
                        sb.appendln(expressionStrategy.getValue(quotedExpr), ";");
                        return null;
                    },
                    stubExpr -> {
                        mockStrategy.stub(sb, "_", stubExpr, expressionStrategy);
                        return null;
                    },
                    matcherExpr -> {
                        matcherExpr.getExpr(sb, "_", expressionStrategy, mockStrategy);
                        return null;
                    });
            sb.appendln(name, " = null;");
        sb.unshift();
        if (expressionStrategy.wrappingException().isPresent()) {
            sb.appendln("} catch (", expressionStrategy.wrappingException().get(), " e) {");
            sb.shift();
                sb.appendln(name, " = ", expressionStrategy.wrappedException("e"), ";");
            sb.unshift();
        }
        sb.appendln("} catch (Throwable e) {");
        sb.shift();
            sb.appendln(name, " = e;");
        sb.unshift();
        sb.appendln("}");
    }

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return union(
                value.accept(
                        quotedExpr -> expressionStrategy.getValueImports(), 
                        stubExpr -> mockStrategy.stubImports(stubExpr, expressionStrategy),
                        matcherExpr -> matcherExpr.importedNames(expressionStrategy, mockStrategy)),
                union(
                        expressionStrategy.wrappingExceptionImports(), 
                        expressionStrategy.wrappedExceptionImports()));
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitBindThrownStatement(this);
    }
    
}
