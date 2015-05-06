package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.ast.Kind;
import yokohama.unit.position.Span;

@Value
public class PrimitiveType implements NonArrayType {
    private Kind kind;

    @Override
    public String getText() {
        return kind.name().toLowerCase();
    }

    public ClassType box() {
        switch (kind) {
            case BOOLEAN: return new ClassType(Boolean.class);
            case BYTE:    return new ClassType(Byte.class);
            case SHORT:   return new ClassType(Short.class);
            case INT:     return new ClassType(Integer.class);
            case LONG:    return new ClassType(Long.class);
            case CHAR:    return new ClassType(Character.class);
            case FLOAT:   return new ClassType(Float.class);
            case DOUBLE:  return new ClassType(Double.class);
        }
        throw new RuntimeException("should not reach here");
    }

    public Type toType() {
        return new Type(this, 0);
    }

    public static PrimitiveType of(yokohama.unit.ast.PrimitiveType primitiveType) {
        return new PrimitiveType(primitiveType.getKind());
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitPrimitiveType(this);
    }
}
