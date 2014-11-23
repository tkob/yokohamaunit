package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

@Value
public class IsNotStatement implements TestStatement {

    private String subject;
    private String complement;

    @Override
    public Set<ImportedName> importedNames() {
        return new TreeSet<ImportedName>(Arrays.asList(
                new ImportClass("ognl.Ognl"),
                new ImportStatic("org.junit.Assert.assertThat"),
                new ImportStatic("org.hamcrest.CoreMatchers.is"),
                new ImportStatic("org.hamcrest.CoreMatchers.not")
        ));
    }

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("{");
        sb.shift();
        sb.appendln("Object actual = Ognl.getValue(\"", escapeJava(subject), "\", env);");
        sb.appendln("Object unexpected = Ognl.getValue(\"", escapeJava(complement), "\", env);");
        sb.appendln("assertThat(actual, is(not(unexpected)));");
        sb.unshift();
        sb.appendln("}");
    }
}
