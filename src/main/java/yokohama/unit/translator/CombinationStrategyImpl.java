package yokohama.unit.translator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.ListUtils;
import yokohama.unit.util.Lists;
import yokohama.unit.util.Pair;

public class CombinationStrategyImpl implements CombinationStrategy {
    @Override
    public <K, V> List<List<Pair<K, V>>> generate(List<Pair<K, List<V>>> candidates) {
        return comb(candidates).collect(Collectors.toList());
    }

    private <K, V> Stream<List<Pair<K, V>>> comb(List<Pair<K, List<V>>> candidates) {
        /*
        What we will do is:
            (* val comb = fn: ('k * 'v list) list -> ('k * 'v) list list *)
            fun comb [] = [[]]
              | comb ((k,vs)::cs) =
                let val kvs = map (fn v => (k, v)) vs
                    val kvss = comb cs
                    val kvsss = map (fn kvs' => map (fn kv => kv::kvs') kvs) kvss
                in
                  List.concat kvsss
                end
        */
        if (candidates.isEmpty()) return Stream.of(Collections.emptyList());

        K k = candidates.get(0).getFirst();
        List<V> vs = candidates.get(0).getSecond();
        List<Pair<K, List<V>>> cs = candidates.subList(1, candidates.size());
        List<Pair<K, V>> kvs = Lists.map(vs, v -> new Pair<>(k, v));
        Stream<List<Pair<K, V>>> kvss = comb(cs);
        return kvss.flatMap(kvs_ ->
                kvs.stream().map(kv -> ListUtils.union(Arrays.asList(kv), kvs_)));
    }
}
