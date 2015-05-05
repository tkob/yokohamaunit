package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Type {
    private NonArrayType nonArrayType;
    private int dims;
    private Span span;

    public static final Type BOOLEAN = new Type(PrimitiveType.BOOLEAN, 0, Span.dummySpan());
    public static final Type BYTE = new Type(PrimitiveType.BYTE, 0, Span.dummySpan());
    public static final Type SHORT = new Type(PrimitiveType.SHORT, 0, Span.dummySpan());
    public static final Type INT = new Type(PrimitiveType.INT, 0, Span.dummySpan());
    public static final Type LONG = new Type(PrimitiveType.LONG, 0, Span.dummySpan());
    public static final Type CHAR = new Type(PrimitiveType.CHAR, 0, Span.dummySpan());
    public static final Type FLOAT = new Type(PrimitiveType.FLOAT, 0, Span.dummySpan());
    public static final Type DOUBLE = new Type(PrimitiveType.DOUBLE, 0, Span.dummySpan());

    public Type toArray() {
        return new Type(nonArrayType, dims + 1, span);
    }

    public static Type fromClass(Class<?> clazz) {
        if (clazz.isArray()) {
            Type componentType = fromClass(clazz.getComponentType());
            return new Type(
                    componentType.getNonArrayType(),
                    componentType.getDims() + 1,
                    Span.dummySpan());
        } else if (clazz.isPrimitive()) {
            if      (clazz.equals(  Boolean.TYPE)) { return BOOLEAN; }
            else if (clazz.equals(     Byte.TYPE)) { return BYTE;    }
            else if (clazz.equals(    Short.TYPE)) { return SHORT;   }
            else if (clazz.equals(  Integer.TYPE)) { return INT;     }
            else if (clazz.equals(     Long.TYPE)) { return LONG;    }
            else if (clazz.equals(Character.TYPE)) { return CHAR;    }
            else if (clazz.equals(    Float.TYPE)) { return FLOAT;   }
            else if (clazz.equals(   Double.TYPE)) { return DOUBLE;  }
            else { throw new RuntimeException("should not reach here"); }
        } else {
            return new Type(
                    new ClassType(clazz.getCanonicalName(), Span.dummySpan()),
                    0,
                    Span.dummySpan());
        }
    }
}
