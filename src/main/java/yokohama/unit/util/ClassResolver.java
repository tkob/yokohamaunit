package yokohama.unit.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ClassResolver {
    private final ClassLoader classLoader;
    private final Map<String, String> table;

    public ClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.table = Collections.emptyMap();
    }
    
    public ClassResolver() {
        this(ClassLoader.getSystemClassLoader());
    }
    
    public ClassResolver(Iterable<Pair<String, String>> source, ClassLoader classLoader) {
        this.classLoader = classLoader;
        Map<String, String> table = new HashMap<>();
        for (Pair<String, String> kv : source) {
            String key = kv.getFirst();
            String value = kv.getSecond();
            if (key.contains("."))
                throw new IllegalArgumentException("The key '" + key + "' contains a dot.");
            table.put(key, value);
        }
        this.table = table;
    }

    public ClassResolver(Iterable<Pair<String, String>> source) {
        this(source, ClassLoader.getSystemClassLoader());
    }

    public Class<?> lookup(String name) throws ClassNotFoundException {
        if (name.contains(".")) {
            return Class.forName(name, false, classLoader);
        } else if (table.containsKey(name)) {
            return Class.forName(table.get(name), false, classLoader);
        } else {
            try {
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException e) {
                return Class.forName("java.lang." + name, false, classLoader);
            }
        }
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public <R> Stream<R> map(BiFunction<? super String, ? super String, ? extends R> f) {
        return table.keySet().stream().map(key -> f.apply(key, table.get(key)));
    }

    public <R> Stream<R> flatMap(BiFunction<? super String, ? super String, Stream<? extends R>> f) {
        return table.keySet().stream().flatMap(key -> f.apply(key, table.get(key)));
    }
}
