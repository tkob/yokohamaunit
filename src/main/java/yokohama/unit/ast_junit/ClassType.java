package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.ClassResolver;

@Value
public class ClassType implements NonArrayType {
    private Class<?> clazz;

    public static final ClassType CLASS = new ClassType(Class.class);
    public static final ClassType STRING = new ClassType(String.class);
    public static final ClassType THROWABLE = new ClassType(Throwable.class);
    public static final ClassType EXCEPTION = new ClassType(Exception.class);

    public static final ClassType BOOLEAN   = new ClassType(Boolean.class);
    public static final ClassType BYTE      = new ClassType(Byte.class);
    public static final ClassType SHORT     = new ClassType(Short.class);
    public static final ClassType INTEGER   = new ClassType(Integer.class);
    public static final ClassType LONG      = new ClassType(Long.class);
    public static final ClassType CHARACTER = new ClassType(Character.class);
    public static final ClassType FLOAT     = new ClassType(Float.class);
    public static final ClassType DOUBLE    = new ClassType(Double.class);

    @Override
    public String getText() {
        return clazz.getCanonicalName();
    }

    @Override
    public String getTypeName() {
        return clazz.getTypeName();
    }

    public boolean isInterface() {
        return clazz.isInterface();
    }

    public static ClassType of(
            yokohama.unit.ast.ClassType classType,
            ClassResolver classResolver) {
        return new ClassType(classType.toClass(classResolver));
    }

    public static ClassType fromClass(Class<?> clazz) {
        return new ClassType(clazz);
    }

    @Override
    public <T> T accept(NonArrayTypeVisitor<T> visitor) {
        return visitor.visitClassType(this);
    }
}
