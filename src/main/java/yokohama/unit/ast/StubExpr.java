package yokohama.unit.ast;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(exclude={"span"})
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class StubExpr implements Expr {
    private QuotedExpr classToStub;
    private List<StubBehavior> behavior;
    private Span span;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitStubExpr(this);
    }
    
}
