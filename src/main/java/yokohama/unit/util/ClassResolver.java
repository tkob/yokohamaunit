package yokohama.unit.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javaslang.Tuple2;
import lombok.Getter;

public class ClassResolver {
    @Getter private final ClassLoader classLoader;
    private final Map<String, String> table;

    public ClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.table = Collections.emptyMap();
    }
    
    public ClassResolver() {
        this(ClassLoader.getSystemClassLoader());
    }
    
    public ClassResolver(Iterable<Tuple2<String, String>> source, ClassLoader classLoader) {
        this.classLoader = classLoader;
        Map<String, String> table = new HashMap<>();
        for (Tuple2<String, String> kv : source) {
            String key = kv._1();
            String value = kv._2();
            if (key.contains("."))
                throw new IllegalArgumentException("The key '" + key + "' contains a dot.");
            table.put(key, value);
        }
        this.table = table;
    }

    public ClassResolver(Iterable<Tuple2<String, String>> source) {
        this(source, ClassLoader.getSystemClassLoader());
    }

    public ClassResolver(Stream<Tuple2<String, String>> source, ClassLoader classLoader) {
        this(source.collect(Collectors.toList()), classLoader);
    }

    public ClassResolver(Stream<Tuple2<String, String>> source) {
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
