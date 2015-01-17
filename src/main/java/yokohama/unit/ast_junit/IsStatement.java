package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import yokohama.unit.util.SBuilder;


@Value
public class IsStatement implements Statement {
    private Var subject;
    private Var complement;

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        Set<ImportedName> importedNames = new TreeSet<ImportedName>(Arrays.asList(
                new ImportStatic("org.junit.Assert.assertThat"),
                new ImportStatic("org.hamcrest.CoreMatchers.is")));
        importedNames.addAll(expressionStrategy.getValueImports());
        return importedNames;
    }

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("assertThat(", subject.getName(), ", is(", complement.getName(), "));");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitIsStatement(this);
    }
}
