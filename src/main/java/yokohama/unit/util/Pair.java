package yokohama.unit.util;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Value;

@Value(staticConstructor="of")
public class Pair<T, U> {
    private final T first;
    private final U second;

    public <R> R map(BiFunction<T, U, R> f) {
        return f.apply(first, second);
    }

    public static <T, U> List<Pair<T, U>> zip(List<T> firsts, List<U> seconds) {
        if (firsts.size() != seconds.size())
            throw new IllegalArgumentException("List size not match");

        return IntStream.range(0, firsts.size())
                .mapToObj(i -> new Pair<T, U>(firsts.get(i), seconds.get(i)))
                .collect(Collectors.toList());
    }

    public static <T, U> Pair<List<T>, List<U>> unzip(List<Pair<T, U>> list) {
        List<T> firsts =
                list.stream().map(Pair::getFirst).collect(Collectors.toList());
        List<U> seconds =
                list.stream().map(Pair::getSecond).collect(Collectors.toList());

        return new Pair<>(firsts, seconds);
    }
}
