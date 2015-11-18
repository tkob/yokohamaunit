package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import lombok.Value;
import yokohama.unit.position.Span;
import yokohama.unit.util.SBuilder;


@Value
public class IsStatement implements Statement {
    private Sym message;
    private Sym subject;
    private Sym complement;
    private Span span;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln(
                "org.junit.Assert.assertThat(",
                message.getName(),
                ", ",
                subject.getName(),
                ", org.hamcrest.CoreMatchers.is(",
                complement.getName(),
                "));");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitIsStatement(this);
    }
}
