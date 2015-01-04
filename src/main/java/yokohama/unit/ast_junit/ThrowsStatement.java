package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;


@Value    
public class ThrowsStatement implements TestStatement {
    private QuotedExpr subject;
    private QuotedExpr complement;

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy) {
        Set<ImportedName> importedNames = new TreeSet<ImportedName>(Arrays.asList(
                new ImportStatic("org.hamcrest.CoreMatchers.instanceOf"),
                new ImportStatic("org.hamcrest.CoreMatchers.is"),
                new ImportStatic("org.junit.Assert.assertThat"),
                new ImportStatic("org.junit.Assert.fail")));
        importedNames.addAll(expressionStrategy.getValueImports());
        importedNames.addAll(expressionStrategy.wrappingExceptionImports());
        importedNames.addAll(expressionStrategy.wrappedExceptionImports());
        return importedNames;
    }

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln("try {");
        sb.shift();
            sb.appendln("Ognl.getValue(\"", escapeJava(subject.getText()), "\", env);");
            sb.appendln("fail(\"`", subject.getText(), "` was expected to throw ", complement.getText(), ".\");");
        sb.unshift();
        if (expressionStrategy.wrappingException().isPresent()) {
            sb.appendln("} catch (", expressionStrategy.wrappingException().get(), " e) {");
            sb.shift();
                sb.appendln("assertThat(", expressionStrategy.wrappedException("e"), ", is(instanceOf(", complement.getText(), ".class)));");
            sb.unshift();
        }
        sb.appendln("} catch (", complement.getText(), " e) {");
        sb.appendln("}");
    }

    @Override
    public <T> T accept(TestStatementVisitor<T> visitor) {
        return visitor.visitThrowsStatement(this);
    }
}
