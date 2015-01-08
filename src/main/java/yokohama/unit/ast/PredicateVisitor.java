package yokohama.unit.ast;

public interface PredicateVisitor<T> {
    T visitIsPredicate(IsPredicate isPredicate);
    T visitIsNotPredicate(IsNotPredicate isNotPredicate);
    T visitThrowsPredicate(ThrowsPredicate throwsPredicate);
}
