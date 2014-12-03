package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import yokohama.unit.util.SBuilder;


@Value
public class IsStatement implements TestStatement {
    private String subject;
    private String complement;

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy) {
        Set<ImportedName> importedNames = new TreeSet<ImportedName>(Arrays.asList(
                new ImportStatic("org.junit.Assert.assertThat"),
                new ImportStatic("org.hamcrest.CoreMatchers.is")));
        importedNames.addAll(expressionStrategy.getValueImports());
        return importedNames;
    }

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln("{");
        sb.shift();
        String actual = expressionStrategy.getValue(subject);
        String expected = expressionStrategy.getValue(complement);
        sb.appendln("Object actual = ", actual, ";");
        sb.appendln("Object expected = ", expected, ";");
        sb.appendln("assertThat(actual, is(expected));");
        sb.unshift();
        sb.appendln("}");
    }

    @Override
    public <T> T accept(TestStatementVisitor<T> visitor) {
        return visitor.visitIsStatement(this);
    }
}
