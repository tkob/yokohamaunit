package yokohama.unit.translator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import yokohama.unit.util.Pair;

public class CombinationStrategyImpl<K, V> implements CombinationStrategy<K, V> {
    @Override
    public List<Map<K, V>> generate(Stream<Pair<K, Collection<V>>> candidates) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
