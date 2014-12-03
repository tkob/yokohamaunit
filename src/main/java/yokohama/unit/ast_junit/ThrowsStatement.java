package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;


@Value    
public class ThrowsStatement implements TestStatement {
    private String subject;
    private String complement;

    @Override
    public Set<ImportedName> importedNames() {
        return new TreeSet<ImportedName>(Arrays.asList(
                new ImportClass("ognl.Ognl"),
                new ImportClass("ognl.OgnlException"),
                new ImportStatic("org.hamcrest.CoreMatchers.instanceOf"),
                new ImportStatic("org.hamcrest.CoreMatchers.is"),
                new ImportStatic("org.junit.Assert.assertThat"),
                new ImportStatic("org.junit.Assert.fail")
                ));
    }

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln("try {");
        sb.shift();
            sb.appendln("Ognl.getValue(\"", escapeJava(subject), "\", env);");
            sb.appendln("fail(\"`", subject, "` was expected to throw ", complement, ".\");");
        sb.unshift();
        sb.appendln("} catch (OgnlException e) {");
        sb.shift();
            sb.appendln("assertThat(e.getReason(), is(instanceOf("+ complement +".class)));");
        sb.unshift();
        sb.appendln("} catch (", complement, " e) {");
        sb.appendln("}");
    }

    @Override
    public <T> T accept(TestStatementVisitor<T> visitor) {
        return visitor.visitThrowsStatement(this);
    }
}
