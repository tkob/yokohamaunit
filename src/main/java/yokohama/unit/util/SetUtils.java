package yokohama.unit.util;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import lombok.NonNull;

public class SetUtils {
    public static <E> Set<E> union(@NonNull Collection<? extends E> a, @NonNull Collection<? extends E> b) {
        Set<E> s = new TreeSet<>();
        s.addAll(a);
        s.addAll(b);
        return s;
    }
}
