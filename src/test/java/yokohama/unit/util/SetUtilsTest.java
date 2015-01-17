package yokohama.unit.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SetUtilsTest {
    @Test
    public void testUnion() {
        Collection<Number> a = new TreeSet<>();
        Collection<Number> b = new TreeSet<>();
        Set<Number> actual = SetUtils.union(a, b);
        Set<Number> expected = new TreeSet<>();
        assertThat(actual, is(expected));
    }

    @Test
    public void testUnion1() {
        Collection<Integer> a = new TreeSet<>(Arrays.asList(1));
        Collection<Number> b = new TreeSet<>();
        Set<Number> actual = SetUtils.union(a, b);
        Set<Number> expected = new TreeSet<>(Arrays.asList(1));
        assertThat(actual, is(expected));
    }

    @Test
    public void testUnion2() {
        Collection<Number> a = new TreeSet<>();
        Collection<Integer> b = new TreeSet<>(Arrays.asList(2));
        Set<Number> actual = SetUtils.union(a, b);
        Set<Number> expected = new TreeSet<>(Arrays.asList(2));
        assertThat(actual, is(expected));
    }

    @Test
    public void testUnion3() {
        Collection<Integer> a = new TreeSet<>(Arrays.asList(1));
        Collection<Integer> b = new TreeSet<>(Arrays.asList(2));
        Set<Number> actual = SetUtils.union(a, b);
        Set<Number> expected = new TreeSet<>(Arrays.asList(2, 1));
        assertThat(actual, is(expected));
    }

    @Test
    public void testUnion4() {
        Collection<Integer> a = new TreeSet<>(Arrays.asList(1, 2));
        Collection<Integer> b = new TreeSet<>(Arrays.asList(2, 3));
        Set<Number> actual = SetUtils.union(a, b);
        Set<Number> expected = new TreeSet<>(Arrays.asList(2, 1, 3));
        assertThat(actual, is(expected));
    }
}
