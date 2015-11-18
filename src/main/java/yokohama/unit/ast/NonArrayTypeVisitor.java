package yokohama.unit.ast;

 public interface NonArrayTypeVisitor<T> {
    T visitPrimitiveType(PrimitiveType primitiveType);
    T visitClassType(ClassType classType);
}
