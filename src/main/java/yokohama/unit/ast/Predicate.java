package yokohama.unit.ast;

import java.util.function.Function;

public interface Predicate extends Describable {
    Span getSpan();

    <T> T accept(PredicateVisitor<T> visitor);

    default <T> T accept(
            Function<IsPredicate, T> visitIsPredicate_,
            Function<IsNotPredicate, T> visitIsNotPredicate_,
            Function<ThrowsPredicate, T> visitThrowsPredicate_
    ) {
        return accept(new PredicateVisitor<T>() {
            @Override
            public T visitIsPredicate(IsPredicate isPredicate) {
                return visitIsPredicate_.apply(isPredicate);
            }
            @Override
            public T visitIsNotPredicate(IsNotPredicate isNotPredicate) {
                return visitIsNotPredicate_.apply(isNotPredicate);
            }
            @Override
            public T visitThrowsPredicate(ThrowsPredicate throwsPredicate) {
                return visitThrowsPredicate_.apply(throwsPredicate);
            }
        });
    }
}
