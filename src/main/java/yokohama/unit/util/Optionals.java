package yokohama.unit.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Optionals {
    public static <T, U> U match(Optional<T> optional, Supplier<U> none, Function<? super T, U> some) {
        return optional.isPresent() ? some.apply(optional.get()) : none.get();
    }

    public static <T> Stream<T> toStream(Optional<T> optional) {
        return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();
    }
}
