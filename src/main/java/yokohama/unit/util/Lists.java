package yokohama.unit.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lists {
    public static <T> List<T> repeat(T obj, int times) {
        @SuppressWarnings("unchecked") T[] array = (T[])new Object[times];
        Arrays.fill(array, obj);
        return Arrays.asList(array);
    }

    public static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> Optional<T> lastOpt(List<T> list) {
        if (list.size() > 0) {
            return Optional.of(Lists.last(list));
        } else {
            return Optional.empty();
        }
    }

    public static <T, U> List<U> mapInitAndLast(
            List<T> list, Function<T, U> initf, Function<T, U> lastf) {
        if (list.isEmpty()) return Collections.emptyList();

        List<T> init = list.subList(0, list.size() - 1);
        T last = last(list);

        return Stream.concat(
                init.stream().map(initf), Stream.of(lastf.apply(last)))
                .collect(Collectors.toList());
    }

    public static <T, U> List<U> flatMapInitAndLast(
            List<T> list,
            Function<T, Stream<U>> initf,
            Function<T, Stream<U>> lastf) {
        if (list.isEmpty()) return Collections.emptyList();

        List<T> init = list.subList(0, list.size() - 1);
        T last = last(list);

        return Stream.concat(init.stream().flatMap(initf), lastf.apply(last))
                .collect(Collectors.toList());
    }
    public static <T, U> List<U> mapFirstAndRest(
            List<T> list, Function<T, U> firstf, Function<T, U> restf) {
        if (list.isEmpty()) return Collections.emptyList();

        T first = list.get(0);
        List<T> rest = list.subList(1, list.size());

        return Stream.concat(
                Stream.of(firstf.apply(first)), rest.stream().map(restf))
                .collect(Collectors.toList());
    }

    public static <T, U> List<U> flatMapFirstAndRest(
            List<T> list,
            Function<T, Stream<U>> firstf,
            Function<T, Stream<U>> restf) {
        if (list.isEmpty()) return Collections.emptyList();

        T first = list.get(0);
        List<T> rest = list.subList(1, list.size());

        return Stream.concat(
                firstf.apply(first), rest.stream().flatMap(restf))
                .collect(Collectors.toList());
    }
}
