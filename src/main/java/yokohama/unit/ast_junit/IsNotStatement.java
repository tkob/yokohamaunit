package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class IsNotStatement implements TestStatement {

    private QuotedExpr subject;
    private QuotedExpr complement;

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy) {
        Set<ImportedName> importedNames = new TreeSet<ImportedName>(Arrays.asList(
                new ImportStatic("org.junit.Assert.assertThat"),
                new ImportStatic("org.hamcrest.CoreMatchers.is"),
                new ImportStatic("org.hamcrest.CoreMatchers.not")));
        importedNames.addAll(expressionStrategy.getValueImports());
        return importedNames;
    }

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln("{");
        sb.shift();
        String actual = expressionStrategy.getValue(subject.getText());
        String unexpected = expressionStrategy.getValue(complement.getText());
        sb.appendln("Object actual = ", actual, ";");
        sb.appendln("Object unexpected = ", unexpected, ";");
        sb.appendln("assertThat(actual, is(not(unexpected)));");
        sb.unshift();
        sb.appendln("}");
    }

    @Override
    public <T> T accept(TestStatementVisitor<T> visitor) {
        return visitor.visitIsNotStatement(this);
    }
}
