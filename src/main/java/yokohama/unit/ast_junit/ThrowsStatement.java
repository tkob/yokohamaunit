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
                new ImportStatic("org.junit.Assert.fail")
                ));
    }

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("try {");
        sb.shift();
            sb.appendln("Ognl.getValue(\"", escapeJava(subject), "\", env);");
            sb.appendln("fail(\"`", subject, "` was expected to throw ", complement, ".\");");
        sb.unshift();
        sb.appendln("} catch (", complement, " e) {");
        sb.appendln("}");
    }
}
