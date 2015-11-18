package yokohama.unit.ast_junit;

import lombok.EqualsAndHashCode;
import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper=false)
public class RegExpMatcherExpr implements Expr {
    String pattern;

    public void getExpr(SBuilder sb, String varName) {
        sb.appendln(
                varName,
                " = com.jcabi.matchers.RegexMatchers.containsPattern(\"",
                escapeJava(pattern),
                "\");");
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitRegExpMatcherExpr(this);
    }
}
