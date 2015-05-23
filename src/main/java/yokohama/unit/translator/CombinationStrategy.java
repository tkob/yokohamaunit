package yokohama.unit.translator;

import java.util.List;
import java.util.Map;
import yokohama.unit.util.Pair;

public interface CombinationStrategy {
    <K, V> List<Map<K, V>> generate(List<Pair<K, List<V>>> candidates);
}
