package yokohama.unit.ast_junit;

import java.util.Optional;
import lombok.SneakyThrows;
import lombok.Value;

@Value
public class ClassType implements NonArrayType {
    private String name;
    private Span span;

    public static final ClassType TEST = new ClassType("org.junit.Test", Span.dummySpan());

    @Override
    public String getText() {
        return name;
    }

    public Type toType() {
        return new Type(this, 0);
    }
    
    @SneakyThrows(ClassNotFoundException.class)
    public Class<?> toClass() {
        return Class.forName(name);
    }

    public static ClassType of(yokohama.unit.ast.ClassType classType) {
        return new ClassType(
                classType.getName(),
                new Span(
                        Optional.empty(),
                        classType.getSpan().getStart(),
                        classType.getSpan().getEnd()));

    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
