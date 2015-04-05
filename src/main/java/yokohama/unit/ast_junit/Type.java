package yokohama.unit.ast_junit;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.ast.Kind;

@Value
public class Type {
    private NonArrayType nonArrayType;
    private int dims;

    public static final Type OBJECT = new Type(new ClassType("java.lang.Object", Span.dummySpan()), 0);
    public static final Type THROWABLE = new Type(new ClassType("java.lang.Throwable", Span.dummySpan()), 0);
    public static final Type CLASS = new Type(new ClassType("java.lang.Class", Span.dummySpan()), 0);
    public static final Type STRING = new Type(new ClassType("java.lang.String", Span.dummySpan()), 0);
    public static final Type MATCHER = new Type(new ClassType("org.hamcrest.Matcher", Span.dummySpan()), 0);
    public static final Type BOOLEAN = new Type(new PrimitiveType(Kind.BOOLEAN), 0);
    public static final Type BYTE = new Type(new PrimitiveType(Kind.BYTE), 0);
    public static final Type SHORT = new Type(new PrimitiveType(Kind.SHORT), 0);
    public static final Type INT = new Type(new PrimitiveType(Kind.INT), 0);
    public static final Type LONG = new Type(new PrimitiveType(Kind.LONG), 0);
    public static final Type CHAR = new Type(new PrimitiveType(Kind.CHAR), 0);
    public static final Type FLOAT = new Type(new PrimitiveType(Kind.FLOAT), 0);
    public static final Type DOUBLE = new Type(new PrimitiveType(Kind.DOUBLE), 0);

    public String getText() {
        return nonArrayType.getText() + StringUtils.repeat("[]", dims);
    }

    public Type box() {
        if (dims > 0) return this;

        return nonArrayType.accept(
                primitiveType ->
                        new Type(primitiveType.box(), dims),
                classType -> this);
    }

    public boolean isPrimitive() {
        return dims == 0 && nonArrayType.accept(
                primitiveType -> true,
                classType -> false);
    }

    public Type toArray() {
        return new Type(nonArrayType, dims + 1);
    }

    public String getFieldDescriptor() {
        String brackets = StringUtils.repeat('[', dims);
        return brackets + nonArrayType.accept(
                primitiveType -> {
                    switch (primitiveType.getKind()) {
                        case BOOLEAN: return "Z";
                        case BYTE:    return "B"; 
                        case SHORT:   return "S";
                        case INT:     return "I";
                        case LONG:    return "J";
                        case CHAR:    return "C";
                        case FLOAT:   return "F";
                        case DOUBLE:  return "D";
                    }
                    throw new RuntimeException("should not reach here");
                },
                classType -> "L" + classType.getText() + ";");
    }

    public Class<?> toClass() {
        if (dims > 0) {
            try {
                return Class.forName(getFieldDescriptor());
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            return nonArrayType.accept(
                    primitiveType -> {
                            switch (primitiveType.getKind()) {
                                case BOOLEAN: return Boolean.TYPE;
                                case BYTE:    return Byte.TYPE;
                                case SHORT:   return Short.TYPE;
                                case INT:     return Integer.TYPE;
                                case LONG:    return Long.TYPE;
                                case CHAR:    return Character.TYPE;
                                case FLOAT:   return Float.TYPE;
                                case DOUBLE:  return Double.TYPE;
                            }
                            throw new RuntimeException("should not reach here");
                    },
                    classType -> classType.toClass());
        }
    }

    public static Type of(yokohama.unit.ast.Type type) {
        return new Type(NonArrayType.of(type.getNonArrayType()), type.getDims());
    }

    public static Type fromClass(Class<?> clazz) {
        if (clazz.isArray()) {
            Type componentType = fromClass(clazz.getComponentType());
            return new Type(componentType.getNonArrayType(), componentType.getDims() + 1);
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
            return new ClassType(clazz.getName(), Span.dummySpan()).toType();
        }
    }
}
