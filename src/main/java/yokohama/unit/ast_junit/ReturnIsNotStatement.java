package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.setOf;

@Value
public class ReturnIsNotStatement implements Statement {
    private Var subject;
    private Var predicate;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("return not(", predicate.getName(), ").matches(", subject.getName(), ");");
    }

    @Override
    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return setOf(new ImportStatic("org.hamcrest.CoreMatchers.not"));
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitReturnIsNotStatement(this);
    }
    
}
