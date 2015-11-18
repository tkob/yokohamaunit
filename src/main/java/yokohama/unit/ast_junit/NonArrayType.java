package yokohama.unit.ast_junit;

import java.util.function.Function;
import yokohama.unit.util.ClassResolver;

public interface NonArrayType {
    String getText();
    String getTypeName();

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

    default public Type toType() {
        return new Type(this, 0);
    }

    public static NonArrayType of(
            yokohama.unit.ast.NonArrayType nonArrayType,
            ClassResolver classResolver) {
        return nonArrayType.accept(
                primitiveType -> PrimitiveType.of(primitiveType), 
                classType -> ClassType.of(classType, classResolver));
    }
}
