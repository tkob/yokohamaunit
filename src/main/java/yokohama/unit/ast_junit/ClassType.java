package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class ClassType implements NonArrayType {
    private String name;
    private Span span;

    public ClassType(String name, Span span) {
        this.name = name;
        this.span = span;
    }

    public ClassType(String name) {
        this.name = name;
        this.span = Span.dummySpan();
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
