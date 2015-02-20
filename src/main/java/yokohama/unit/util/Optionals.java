package yokohama.unit.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Optionals {
    public static <T, U> U match(Optional<T> optional, Supplier<U> none, Function<? super T, U> some) {
        return optional.isPresent() ? some.apply(optional.get()) : none.get();
    }
}
