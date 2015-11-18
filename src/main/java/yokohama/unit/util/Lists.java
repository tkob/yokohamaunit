package yokohama.unit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
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

    public static <T> Pair<List<T>, List<T>> split(List<T> list, int pos) {
        List<T> left = list.subList(0, pos);
        List<T> right = list.subList(pos, list.size());
        return Pair.of(left, right);
    }

    public static <T, U> List<U> map(List<T> list, Function<T, U> f) {
        return list.stream().map(f).collect(Collectors.toList());
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

    public static <T> void forEachOrderedInitAndLast(
            List<T> list, Consumer<T> initf, Consumer<T> lastf) {
        if (list.isEmpty()) return;

        List<T> init = list.subList(0, list.size() - 1);
        T last = last(list);

        init.stream().forEachOrdered(initf);
        lastf.accept(last);
    }

    public static <T> void forEachOrderedFirstAndRest(
            List<T> list, Consumer<T> firstf, Consumer<T> restf) {
        if (list.isEmpty()) return;

        T first = list.get(0);
        List<T> rest = list.subList(1, list.size());

        firstf.accept(first);
        rest.stream().forEachOrdered(restf);
    }

    public static <T, U, V> Map<U, V> listToMap(
            List<T> list, Function<T, Pair<U, V>> f) {
        return list.stream().collect(
                () -> new HashMap<>(),
                (map, e) -> {
                    Pair<U, V> pair = f.apply(e);
                    map.put(pair.getFirst(), pair.getSecond());
                },
                (map1, map2) -> map1.putAll(map2));
    }

    public static <T> List<T> concat(List<T>... lists) {
        List<T> newList = new ArrayList<>();
        for (List<T> list : lists) {
            newList.addAll(list);
        }
        return newList;
    }

    public static <T> List<T> fromStreams(Stream<T>... streams) {
        Stream<T> newStream = Stream.empty();
        for (Stream<T> stream : streams) {
            newStream = Stream.concat(newStream, stream);
        }
        return newStream.collect(Collectors.toList());
    }
}
