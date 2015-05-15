package yokohama.unit.ast;

import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Invoke implements Statement {
    ClassType classType;
    MethodPattern methodPattern;
    Optional<Expr> receiver;
    List<Expr> args;
    Span span;
    
    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitInvoke(this);
    }
}
