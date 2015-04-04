package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.ast.Kind;

@Value
public class PrimitiveType implements NonArrayType {
    private Kind kind;

    @Override
    public String getText() {
        return kind.name().toLowerCase();
    }

    public ClassType box() {
        switch (kind) {
            case BOOLEAN:
                return new ClassType("java.lang.Boolean", Span.dummySpan());
            case BYTE:
                return new ClassType("java.lang.Byte", Span.dummySpan());
            case SHORT:
                return new ClassType("java.lang.Short", Span.dummySpan());
            case INT:
                return new ClassType("java.lang.Integer", Span.dummySpan());
            case LONG:
                return new ClassType("java.lang.Long", Span.dummySpan());
            case CHAR:
                return new ClassType("java.lang.Character", Span.dummySpan());
            case FLOAT:
                return new ClassType("java.lang.Float", Span.dummySpan());
            case DOUBLE:
                return new ClassType("java.lang.Double", Span.dummySpan());
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
