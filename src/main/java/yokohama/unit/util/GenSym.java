package yokohama.unit.util;

import java.util.HashMap;
import java.util.Map;

public class GenSym {
    private final Map<String, Integer> map = new HashMap<>();

    public String generate(String prefix) {
        int index = map.compute(prefix, (k, v) -> v == null ? 1 : v + 1);
        return index == 1 ? prefix : prefix + index;
    }
    
}
