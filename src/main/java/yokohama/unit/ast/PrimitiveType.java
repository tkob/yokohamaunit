package yokohama.unit.ast;

import lombok.Value;

@Value
public class PrimitiveType implements NonArrayType {
    private Kind kind;

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitPrimitiveType(this);
    }
}
