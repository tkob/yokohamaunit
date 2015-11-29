package yokohama.unit.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javaslang.Tuple;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ClassResolverTest {
    @Test
    public void testLookup() throws Exception {
        ClassResolver instance = new ClassResolver();
        Class<?> expected = java.util.ArrayList.class;
        Class<?> actual = instance.lookup("java.util.ArrayList");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testLookup2() throws Exception {
        ClassResolver instance = new ClassResolver(Arrays.asList(Tuple.of("ArrayList", "java.util.ArrayList")));
        Class<?> expected = java.util.ArrayList.class;
        Class<?> actual = instance.lookup("ArrayList");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testLookup3() throws Exception {
        ClassResolver instance = new ClassResolver();
        Class<?> expected = java.lang.String.class;
        Class<?> actual = instance.lookup("String");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testLookup4() throws Exception {
        ClassResolver instance = new ClassResolver(Arrays.asList(Tuple.of("String", "java.util.ArrayList")));
        Class<?> expected = java.util.ArrayList.class;
        Class<?> actual = instance.lookup("String");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testIsEmpty() {
        ClassResolver instance = new ClassResolver();
        boolean expected = true;
        boolean actual = instance.isEmpty();
        assertThat(actual, is(expected));
    }

    @Test
    public void testIsEmpty2() {
        ClassResolver instance = new ClassResolver(Arrays.asList(Tuple.of("ArrayList", "java.util.ArrayList")));
        boolean expected = false;
        boolean actual = instance.isEmpty();
        assertThat(actual, is(expected));
    }

    @Test
    public void testMap() {
        ClassResolver instance = new ClassResolver();
        List<String> expected = Arrays.asList();
        List<String> actual = instance.map((k, v) -> k + ", " + v).collect(Collectors.toList());
        assertThat(actual, is(expected));
    }

    @Test
    public void testMap2() {
        ClassResolver instance = new ClassResolver(Arrays.asList(Tuple.of("ArrayList", "java.util.ArrayList")));
        List<String> expected = Arrays.asList("ArrayList, java.util.ArrayList");
        List<String> actual = instance.map((k, v) -> k + ", " + v).collect(Collectors.toList());
        assertThat(actual, is(expected));
    }

    @Test
    public void testFlatMap() {
        ClassResolver instance = new ClassResolver();
        List<String> expected = Arrays.asList();
        List<String> actual = instance.flatMap((k, v) -> Stream.of(k, v)).collect(Collectors.toList());
        assertThat(actual, is(expected));
    }

    @Test
    public void testFlatMap2() {
        ClassResolver instance = new ClassResolver(Arrays.asList(Tuple.of("ArrayList", "java.util.ArrayList")));
        List<String> expected = Arrays.asList("ArrayList", "java.util.ArrayList");
        List<String> actual = instance.flatMap((k, v) -> Stream.of(k, v)).collect(Collectors.toList());
        assertThat(actual, is(expected));
    }
}
