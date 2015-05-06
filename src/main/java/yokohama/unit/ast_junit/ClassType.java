package yokohama.unit.ast_junit;

import java.util.Optional;
import lombok.Value;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;

@Value
public class ClassType implements NonArrayType {
    private Class<?> clazz;
    private Span span;

    public static final ClassType TEST = new ClassType(org.junit.Test.class, Span.dummySpan());
    public static final ClassType STRING = new ClassType(String.class, Span.dummySpan());

    public static final ClassType BOOLEAN   = new ClassType(Boolean.class,   Span.dummySpan());
    public static final ClassType BYTE      = new ClassType(Byte.class,      Span.dummySpan());
    public static final ClassType SHORT     = new ClassType(Short.class,     Span.dummySpan());
    public static final ClassType INTEGER   = new ClassType(Integer.class,   Span.dummySpan());
    public static final ClassType LONG      = new ClassType(Long.class,      Span.dummySpan());
    public static final ClassType CHARACTER = new ClassType(Character.class, Span.dummySpan());
    public static final ClassType FLOAT     = new ClassType(Float.class,     Span.dummySpan());
    public static final ClassType DOUBLE    = new ClassType(Double.class,    Span.dummySpan());

    @Override
    public String getText() {
        return clazz.getCanonicalName();
    }

    public boolean isInterface() {
        return clazz.isInterface();
    }

    public Type toType() {
        return new Type(this, 0);
    }
    
    public static ClassType of(
            yokohama.unit.ast.ClassType classType,
            ClassResolver classResolver) {
        return new ClassType(
                classType.toClass(classResolver),
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
