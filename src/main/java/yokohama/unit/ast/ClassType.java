package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.ClassResolver;

@Value
@EqualsAndHashCode(exclude={"span"})
public class ClassType implements NonArrayType {
    private String name;
    private Span span;

    public String getCanonicalName(ClassResolver classResolver) {
        try {
            return classResolver.lookup(name).getCanonicalName();
        } catch (ClassNotFoundException e) {
            throw new AstException(e.getMessage(), span, e);
        }
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
