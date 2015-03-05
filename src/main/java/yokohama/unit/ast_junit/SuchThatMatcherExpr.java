package yokohama.unit.ast_junit;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper=false)
public class SuchThatMatcherExpr extends MatcherExpr {
    private List<Statement> statements;
    private String description;
    private Var argVar;

    @Override
    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("org.hamcrest.Matcher ", varName, " = new org.hamcrest.BaseMatcher() {");
        sb.shift();
            sb.appendln("@Override");
            sb.appendln("public boolean matches(Object ", argVar.getName(), ") {");
            sb.shift();
                sb.appendln("try {");
                sb.shift();
                for (Statement statement : statements) {
                    statement.toString(sb, expressionStrategy, mockStrategy);
                }
                sb.unshift();
                sb.appendln("} catch (Exception $e) {"); // TODO: var name should be gensym'ed
                sb.shift();
                    sb.appendln("throw new RuntimeException($e);");
                sb.unshift();
                sb.appendln("}");
            sb.unshift();
            sb.appendln("}");
            sb.appendln("@Override");
            sb.appendln("public void describeTo(org.hamcrest.Description description) {");
            sb.shift();
                sb.appendln("description.appendText(\"",  escapeJava(description), "\");");
            sb.unshift();
            sb.appendln("}");
        sb.unshift();
        sb.appendln("};");
    }
}
