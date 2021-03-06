package yokohama.unit.ast;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.Lists;

@Value
@EqualsAndHashCode(exclude={"span"})
public class MethodPattern {
    private String name;
    private List<Type> paramTypes;
    private boolean vararg;
    private Span span;

    public Optional<Type> getReturnType(
            ClassType classType, ClassResolver classResolver) {
        Class<?> clazz = classType.toClass(classResolver);
        List<Type> argTypesVarArgErased = vararg
                ? Lists.mapInitAndLast(paramTypes, Function.identity(),
                        type -> type.toArray())
                : paramTypes;
        try {
            Method method = clazz.getMethod(
                    name,
                    argTypesVarArgErased.stream()
                            .map(type -> type.toClass(classResolver))
                            .toArray(n -> new Class[n]));
            Class<?> returnType = method.getReturnType();
            return returnType.equals(Void.TYPE)
                    ? Optional.empty()
                    : Optional.of(Type.fromClass(returnType));
        } catch (NoSuchMethodException e) {
            throw new AstException(e.getMessage(), span, e);
        }
    }
}
