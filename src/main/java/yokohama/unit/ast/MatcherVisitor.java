package yokohama.unit.ast;

public interface MatcherVisitor<T> {
    T visitEqualTo(EqualToMatcher equalTo);
    T visitInstanceOf(InstanceOfMatcher instanceOf);
    T visitInstanceSuchThat(InstanceSuchThatMatcher instanceSuchThat);
    T visitNullValue(NullValueMatcher nullValue);
}
