package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class PrimitiveType implements NonArrayType {
    private Kind kind;
    private Span span;

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitPrimitiveType(this);
    }
}
