package yokohama.unit.ast;

import lombok.Value;

@Value
public class PrimitiveType implements NonArrayType {
    public enum Kind {
        BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE
    }

    private Kind kind;

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitPrimitiveType(this);
    }
}
