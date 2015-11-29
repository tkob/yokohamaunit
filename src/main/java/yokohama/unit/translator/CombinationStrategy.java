package yokohama.unit.translator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javaslang.Tuple2;

public interface CombinationStrategy {
    <K, V> List<List<Tuple2<K, V>>> generate(List<Tuple2<K, List<V>>> candidates);

    default <K, V> List<Map<K, V>> generateMap(List<Tuple2<K, List<V>>> candidates) {
        return generate(candidates).stream()
                .map(kvs ->
                        kvs.stream().<Map<K, V>>collect(
                                () -> new HashMap(),
                                (m, kv) -> m.put(kv._1(), kv._2()),
                                (m1, m2) -> m1.putAll(m2)))
                .collect(Collectors.toList());
    }
}
