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
    public static final Type STRING = new Type(new ClassType("java.lang.String", Span.dummySpan()), 0);
    public static final Type MATCHER = new Type(new ClassType("org.hamcrest.Matcher", Span.dummySpan()), 0);
    public static final Type INT = new Type(new PrimitiveType(Kind.INT), 0);

    public String getText() {
        return nonArrayType.getText() + StringUtils.repeat("[]", dims);
    }
}
