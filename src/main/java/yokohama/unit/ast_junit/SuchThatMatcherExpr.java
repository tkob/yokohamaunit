package yokohama.unit.ast_junit;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.Pair;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper=false)
public class SuchThatMatcherExpr implements Expr {
    private List<Statement> statements;
    private String description;
    private Var argVar;

    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln(varName, " = new org.hamcrest.BaseMatcher() {");
        sb.shift();
            sb.appendln("@Override");
            sb.appendln("public boolean matches(Object ", argVar.getName(), ") {");
            sb.shift();
                for (Pair<Type, String> pair : VarDeclVisitor.sortedSet(new VarDeclVisitor().visitStatements(statements))) {
                    Type type = pair.getFirst();
                    String name = pair.getSecond();
                    sb.appendln(type.getText(), " ", name, ";");
                }
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

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitSuchThatMatcherExpr(this);
    }
}
