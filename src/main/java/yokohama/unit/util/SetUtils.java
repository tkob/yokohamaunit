package yokohama.unit.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

public class SetUtils {
    public static <E> Set<E> union(@NonNull Collection<? extends E> a, @NonNull Collection<? extends E> b) {
        Set<E> s = new HashSet<>();
        s.addAll(a);
        s.addAll(b);
        return s;
    }

    @SafeVarargs
    public static <E> Set<E> setOf(E... elements) {
        return Arrays.stream(elements).collect(Collectors.toSet());
    }
}
