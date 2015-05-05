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

    public String getFieldDescriptor() {
        switch (kind) {
            case BOOLEAN: return "Z";
            case BYTE:    return "B"; 
            case SHORT:   return "S";
            case INT:     return "I";
            case LONG:    return "J";
            case CHAR:    return "C";
            case FLOAT:   return "F";
            case DOUBLE:  return "D";
        }
        throw new AstException(
                "should not reach here: cannot get descriptor of " + kind,
                span);
    }

    public Class<?> toClass() {
        switch (kind) {
            case BOOLEAN: return Boolean.TYPE;
            case BYTE:    return Byte.TYPE;
            case SHORT:   return Short.TYPE;
            case INT:     return Integer.TYPE;
            case LONG:    return Long.TYPE;
            case CHAR:    return Character.TYPE;
            case FLOAT:   return Float.TYPE;
            case DOUBLE:  return Double.TYPE;
        }
        throw new AstException(
                "should not reach here: cannot convert " + kind + " to Class",
                span);
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitPrimitiveType(this);
    }
}
