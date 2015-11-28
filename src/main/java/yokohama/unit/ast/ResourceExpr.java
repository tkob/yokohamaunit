package yokohama.unit.ast;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ResourceExpr implements Expr {
    String name;
    Optional<ClassType> classType;
    Span span;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitResourceExpr(this);
    }
}
