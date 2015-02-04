package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.setOf;

@Value
@EqualsAndHashCode(callSuper=false)
public class SuchThatMatcherExpr extends MatcherExpr {
    private List<Statement> statements;

    @Override
    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("Matcher ", varName, " = new BaseMatcher() {");
        sb.shift();
            sb.appendln("@Override");
            sb.appendln("public boolean matches(Object obj) {");
            sb.shift();
            for (Statement statement : statements) {
                statement.toString(sb, expressionStrategy, mockStrategy);
            }
            sb.unshift();
            sb.appendln("}");
            sb.appendln("@Override");
            sb.appendln("public void describeTo(Description description) {");
            sb.appendln("}");
        sb.unshift();
        sb.appendln("};");
    }

    @Override
    public Set<ImportedName> importedNames() {
        return setOf(
                new ImportClass("org.hamcrest.BaseMatcher"),
                new ImportClass("org.hamcrest.Description"));
    }
}
