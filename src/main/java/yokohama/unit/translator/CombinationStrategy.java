package yokohama.unit.translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import yokohama.unit.util.Pair;

public interface CombinationStrategy {
    <K, V> List<List<Pair<K, V>>> generate(List<Pair<K, List<V>>> candidates);

    default <K, V> List<Map<K, V>> generateMap(List<Pair<K, List<V>>> candidates) {
        return generate(candidates).stream()
                .map(kvs ->
                        kvs.stream().<Map<K, V>>collect(
                                () -> new HashMap(),
                                (m, kv) -> m.put(kv.getFirst(), kv.getSecond()),
                                (m1, m2) -> m1.putAll(m2)))
                .collect(Collectors.toList());
    }
}
