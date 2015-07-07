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
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;

public class JCUnitIPO2CombinationStrategy implements CombinationStrategy {
    public <K, V> List<List<Pair<K, V>>> generate(List<Pair<K, List<V>>> candidates) {
        GenSym genSym = new GenSym();
        Map<String, K> map = new HashMap<>();
        Factors.Builder factorsBuilder = new Factors.Builder();
        for (Pair<K, List<V>> candidate : candidates) {
            K key = candidate.getFirst();
            String sym = genSym.generate(key.toString()).getName();
            map.put(sym, key);
            Factor.Builder factorBuilder = new Factor.Builder();
            factorBuilder.setName(sym);
            for (V v : candidate.getSecond()) {
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
        List<List<Pair<K, V>>> tuples =
                StreamSupport.stream(tg.spliterator(), false)
                        .map(tuple -> {
                            Set<String> keys = tuple.keySet();
                            return keys.stream()
                                    .map(key -> {
                                        @SuppressWarnings("unchecked") V v =
                                                (V)tuple.get(key);
                                        return new Pair<>(map.get(key), v);
                                    }).collect(Collectors.toList());
                        }).collect(Collectors.toList());
        return tuples;
    }
}
