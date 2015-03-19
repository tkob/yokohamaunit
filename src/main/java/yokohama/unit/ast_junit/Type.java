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

    public static Type of(yokohama.unit.ast.Type type) {
        return new Type(NonArrayType.of(type.getNonArrayType()), type.getDims());
    }
}
