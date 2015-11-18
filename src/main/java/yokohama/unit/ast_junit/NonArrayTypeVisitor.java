package yokohama.unit.ast_junit;

 public interface NonArrayTypeVisitor<T> {
    T visitPrimitiveType(PrimitiveType primitiveType);
    T visitClassType(ClassType classType);
}
