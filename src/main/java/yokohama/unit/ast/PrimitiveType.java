package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class PrimitiveType implements NonArrayType {
    private Kind kind;
    private Span span;

    public static final PrimitiveType BOOLEAN = new PrimitiveType(Kind.BOOLEAN, Span.dummySpan());
    public static final PrimitiveType BYTE = new PrimitiveType(Kind.BYTE, Span.dummySpan());
    public static final PrimitiveType SHORT = new PrimitiveType(Kind.SHORT, Span.dummySpan());
    public static final PrimitiveType INT = new PrimitiveType(Kind.INT, Span.dummySpan());
    public static final PrimitiveType LONG = new PrimitiveType(Kind.LONG, Span.dummySpan());
    public static final PrimitiveType CHAR = new PrimitiveType(Kind.CHAR, Span.dummySpan());
    public static final PrimitiveType FLOAT = new PrimitiveType(Kind.FLOAT, Span.dummySpan());
    public static final PrimitiveType DOUBLE = new PrimitiveType(Kind.DOUBLE, Span.dummySpan());

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitPrimitiveType(this);
    }
}
