package yokohama.unit.ast_junit;

import java.util.List;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class TryStatement implements Statement {
    private final List<Statement> tryStatements;
    private final List<CatchClause> catchClauses;
    private final List<Statement> finallyStatements;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("try {");
        sb.shift();
        for (Statement statement : tryStatements) {
            statement.toString(sb);
        }
        sb.unshift();
        for (CatchClause catchClause : catchClauses) {
            String classType = catchClause.getClassType().getText();
            String var = catchClause.getVar().getName();
            sb.appendln("} catch(", classType, " ", var, ") {");
            sb.shift();
            for (Statement statement : catchClause.getStatements()) {
                statement.toString(sb);
            }
            sb.unshift();
        }
        if (finallyStatements.size() > 0) {
            sb.appendln("} finally {");
            sb.shift();
            for (Statement statement : finallyStatements) {
                statement.toString(sb);
            }
            sb.unshift();
        }
        sb.appendln("}");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitTryStatement(this);
    }
}
