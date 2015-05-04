package yokohama.unit.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
}
