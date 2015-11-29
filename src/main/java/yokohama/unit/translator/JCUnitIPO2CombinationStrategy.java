package yokohama.unit.translator;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javaslang.Tuple;
import javaslang.Tuple2;
import yokohama.unit.util.GenSym;

public class JCUnitIPO2CombinationStrategy implements CombinationStrategy {
    public <K, V> List<List<Tuple2<K, V>>> generate(List<Tuple2<K, List<V>>> candidates) {
        GenSym genSym = new GenSym();
        Map<String, K> map = new HashMap<>();
        Factors.Builder factorsBuilder = new Factors.Builder();
        for (Tuple2<K, List<V>> candidate : candidates) {
            K key = candidate._1();
            String sym = genSym.generate(key.toString()).getName();
            map.put(sym, key);
            Factor.Builder factorBuilder = new Factor.Builder(sym);
            for (V v : candidate._2()) {
                factorBuilder.addLevel(v);
            }
            Factor factor = factorBuilder.build();
            factorsBuilder.add(factor);
        }
        Factors factors = factorsBuilder.build();
        ConstraintManager cm = new NullConstraintManager();
        TupleGenerator tg = new TupleGenerator.Builder()
                .setTupleGeneratorClass(IPO2TupleGenerator.class)
                .setConstraintManager(cm)
                .setFactors(factors)
                .setParameters(new Param[0])
                .build();
        List<List<Tuple2<K, V>>> tuples =
                StreamSupport.stream(tg.spliterator(), false)
                        .map(tuple -> {
                            Set<String> keys = tuple.keySet();
                            return keys.stream()
                                    .map(key -> {
                                        @SuppressWarnings("unchecked") V v =
                                                (V)tuple.get(key);
                                        return Tuple.of(map.get(key), v);
                                    }).collect(Collectors.toList());
                        }).collect(Collectors.toList());
        return tuples;
    }
}
