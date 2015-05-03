package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.ClassResolver;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ClassType implements NonArrayType {
    private String name; // may be fully qualified name or abbreviation
    private Span span;

    public Class<?> toClass(ClassResolver classResolver) {
        try {
            return classResolver.lookup(name);
        } catch (ClassNotFoundException e) {
            throw new AstException(e.getMessage(), span, e);
        }
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
