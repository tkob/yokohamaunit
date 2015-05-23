package yokohama.unit.translator;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import yokohama.unit.util.Pair;

public interface CombinationStrategy<K, V> {
    Map<K, V> generate(Stream<Pair<K, Collection<V>>> candidates);
}
