package yokohama.unit.ast_junit;

import java.util.function.Function;

public interface NonArrayType {
    String getText();

    <T> T accept(NonArrayTypeVisitor<T> visitor);

    default <T> T accept(
            Function<PrimitiveType, T> visitPrimitiveType_,
            Function<ClassType, T> visitClassType_
    ) {
        return accept(new NonArrayTypeVisitor<T>() {
            @Override
            public T visitPrimitiveType(PrimitiveType primitiveType) {
                return visitPrimitiveType_.apply(primitiveType);
            }
            @Override
            public T visitClassType(ClassType classType) {
                return visitClassType_.apply(classType);
            }
        });
    }
}
