package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class IsNotStatement implements Statement {
    private Var subject;
    private Var complement;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("org.junit.Assert.assertThat(", subject.getName(), ", org.hamcrest.CoreMatchers.is(org.hamcrest.CoreMatchers.not(", complement.getName(), ")));");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitIsNotStatement(this);
    }
}
