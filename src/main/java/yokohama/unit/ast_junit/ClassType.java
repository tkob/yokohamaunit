package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class ClassType implements NonArrayType {
    private String name;
    private Span span;

    @Override
    public String getText() {
        return name;
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
