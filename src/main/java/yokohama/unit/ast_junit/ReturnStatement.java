package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ReturnStatement implements Statement {
    Sym returned;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("return " + returned.getName() + ";");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitReturnStatement(this);
    }
    
}
