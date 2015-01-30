package yokohama.unit.util;

import lombok.Value;

@Value
public class Pair<T, U> {
    private final T first;
    private final U second;
}
