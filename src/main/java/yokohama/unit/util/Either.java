package yokohama.unit.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Value;

abstract public class Either<T, U> {
    public abstract <V> V match(Function<T, V> left, Function<U, V> right);
    public abstract boolean isLeft();
    public abstract boolean isRight();

    public Optional<T> leftOptional() {
        return this.match(l -> Optional.of(l), r -> Optional.<T>empty());
    }

    public Optional<U> rightOptional() {
        return this.match(l -> Optional.<U>empty(), r -> Optional.of(r));
    }

    public Stream<T> leftStream() {
        return this.match(l -> Stream.of(l), r -> Stream.<T>empty());
    }

    public Stream<U> rightStream() {
        return this.match(l -> Stream.<U>empty(), r -> Stream.of(r));
    }

    public static <T, U> Either<T, U> left(T value) { return new Left<>(value); }
    public static <T, U> Either<T, U> right(U value) { return new Right<>(value); }

    @Value
    @EqualsAndHashCode(callSuper=false)
    private static class Left<T, U> extends Either<T, U> {
        private final T value;

        @Override
        public <V> V match(Function<T, V> left, Function<U, V> right) {
            return left.apply(value);
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper=false)
    private static class Right<T, U> extends Either<T, U> {
        private final U value;

        @Override
        public <V> V match(Function<T, V> left, Function<U, V> right) {
            return right.apply(value);
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }
    }
}
