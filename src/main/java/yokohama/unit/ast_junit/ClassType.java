package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class ClassType implements NonArrayType {
    private String name;
    private Span span;

    public static final ClassType OBJECT = new ClassType("java.lang.Object", Span.dummySpan());
    public static final ClassType THROWABLE = new ClassType("java.lang.Throwable", Span.dummySpan());
    public static final ClassType STRING = new ClassType("java.lang.String", Span.dummySpan());
    public static final ClassType MATCHER = new ClassType("org.hamcrest.Matcher", Span.dummySpan());

    @Override
    public String getText() {
        return name;
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
