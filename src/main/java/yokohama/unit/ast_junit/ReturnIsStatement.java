package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ReturnIsStatement implements Statement {
    private Var subject;
    private Var predicate;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("return ", predicate.getName(), ".matches(", subject.getName(), ");");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitReturnIsStatement(this);
    }
    
}
