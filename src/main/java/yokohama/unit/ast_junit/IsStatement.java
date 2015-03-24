package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;


@Value
public class IsStatement implements Statement {
    private Var subject;
    private Var complement;
    private Span span;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("org.junit.Assert.assertThat(", subject.getName(), ", org.hamcrest.CoreMatchers.is(", complement.getName(), "));");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitIsStatement(this);
    }
}
