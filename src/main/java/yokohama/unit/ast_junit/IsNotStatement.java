package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.setOf;
import static yokohama.unit.util.SetUtils.union;

@Value
public class IsNotStatement implements Statement {
    private Var subject;
    private Var complement;

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return union(
                expressionStrategy.getValueImports(),
                setOf(
                        new ImportStatic("org.junit.Assert.assertThat"),
                        new ImportStatic("org.hamcrest.CoreMatchers.is"),
                        new ImportStatic("org.hamcrest.CoreMatchers.not")));
    }

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("assertThat(", subject.getName(), ", is(not(", complement.getName(), ")));");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitIsNotStatement(this);
    }
}
