package yokohama.unit.ast;

import java.util.function.Function;

public interface Matcher {
    <T> T accept(MatcherVisitor<T> visitor);

    default <T> T accept(
            Function<EqualToMatcher, T> visitEqualTo_,
            Function<InstanceOfMatcher, T> visitInstanceOf_,
            Function<InstanceSuchThatMatcher, T> visitInstanceSuchThat_,
            Function<NullValueMatcher, T> visitNullValue_
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
            @Override
            public T visitInstanceSuchThat(InstanceSuchThatMatcher instanceSuchThat) {
                return visitInstanceSuchThat_.apply(instanceSuchThat);
            }
            @Override
            public T visitNullValue(NullValueMatcher nullValue) {
                return visitNullValue_.apply(nullValue);
            }
        });
    }
}
