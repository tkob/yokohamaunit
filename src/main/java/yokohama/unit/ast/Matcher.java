package yokohama.unit.ast;

import java.util.function.Function;

public interface Matcher {
    <T> T accept(MatcherVisitor<T> visitor);

    default <T> T accept(
            Function<EqualToMatcher, T> visitEqualTo_,
            Function<InstanceOfMatcher, T> visitInstanceOf_
    ) {
        return accept(new MatcherVisitor<T>() {
            @Override
            public T visitEqualTo(EqualToMatcher equalTo) {
                return visitEqualTo_.apply(equalTo);
            }
            @Override
            public T visitInstanceOf(InstanceOfMatcher instanceOf) {
                return visitInstanceOf_.apply(instanceOf);
            }
        });
    }
}
