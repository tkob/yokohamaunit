package yokohama.unit.util;

import java.util.Arrays;
import java.util.List;

public class Lists {
    public static <T> List<T> repeat(T obj, int times) {
        Object[] array = new Object[times];
        Arrays.fill(array, obj);
        return Arrays.asList((T[])array);
    }
}
