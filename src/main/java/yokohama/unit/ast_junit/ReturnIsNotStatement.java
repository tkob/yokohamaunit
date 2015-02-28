package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ReturnIsNotStatement implements Statement {
    private Var subject;
    private Var predicate;

    @Override
    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln("return org.hamcrest.CoreMatchers.not(", predicate.getName(), ").matches(", subject.getName(), ");");
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitReturnIsNotStatement(this);
    }
    
}
