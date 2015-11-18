package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;
import yokohama.unit.util.Sym;

@Value
public class ThrowStatement implements Statement {
    Sym e;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("throw ", e.getName(), ";");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitThrowStatement(this);
    }
}