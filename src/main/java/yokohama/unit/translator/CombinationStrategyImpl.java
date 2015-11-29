package yokohama.unit.translator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.apache.commons.collections4.ListUtils;
import yokohama.unit.util.Lists;

public class CombinationStrategyImpl implements CombinationStrategy {
    @Override
    public <K, V> List<List<Tuple2<K, V>>> generate(List<Tuple2<K, List<V>>> candidates) {
        return comb(candidates).collect(Collectors.toList());
    }

    private <K, V> Stream<List<Tuple2<K, V>>> comb(List<Tuple2<K, List<V>>> candidates) {
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

        K k = candidates.get(0)._1();
        List<V> vs = candidates.get(0)._2();
        List<Tuple2<K, List<V>>> cs = candidates.subList(1, candidates.size());
        List<Tuple2<K, V>> kvs = Lists.map(vs, v -> Tuple.of(k, v));
        Stream<List<Tuple2<K, V>>> kvss = comb(cs);
        return kvss.flatMap(kvs_ ->
                kvs.stream().map(kv -> ListUtils.union(Arrays.asList(kv), kvs_)));
    }
}
