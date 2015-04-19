package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import yokohama.unit.util.ClassResolver;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ClassType implements NonArrayType {
    private String name;
    private Span span;

    @SneakyThrows(ClassNotFoundException.class)
    public String getCanonicalName(ClassResolver classResolver) {
        return classResolver.lookup(name).getCanonicalName();
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
