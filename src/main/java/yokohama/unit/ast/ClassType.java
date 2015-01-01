package yokohama.unit.ast;

import lombok.Value;

@Value
public class ClassType implements NonArrayType {
    private String name;

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
