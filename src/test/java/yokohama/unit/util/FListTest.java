package yokohama.unit.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static yokohama.unit.util.FList.append;
import static yokohama.unit.util.FList.cons;
import static yokohama.unit.util.FList.empty;

public class FListTest {
    @Test
    public void testMatch() {
        boolean actual = empty().match(
                () -> true,
                (car, cdr) -> false);
        assertThat(actual, is(true));
    }

    @Test
    public void testMatch1() {
        boolean actual = FList.of(1).match(
                () -> true,
                (car, cdr) -> false);
        assertThat(actual, is(false));
    }

    @Test
    public void testSize() {
        assertThat(empty().size(), is(0));
    }

    @Test
    public void testSize1() {
        assertThat(FList.of(47).size(), is(1));
    }

    @Test
    public void testIsEmpty() {
        FList<Object> instance = empty();
        boolean actual = instance.isEmpty();
        boolean expected = true;
        assertThat(actual, is(expected));
    }

    @Test
    public void testIsEmpty1() {
        FList<String> instance = cons("abc", empty());
        boolean actual = instance.isEmpty();
        boolean expected = false;
        assertThat(actual, is(expected));
    }

    @Test
    public void testContains() {
        assertThat(FList.empty().contains(1), is(false));
    }

    @Test
    public void testContains1() {
        assertThat(FList.of(1).contains(1), is(true));
    }

    @Test
    public void testContains2() {
        assertThat(FList.of(2, 1).contains(1), is(true));
    }

    @Test
    public void testContains3() {
        assertThat(FList.of(2, 3).contains(1), is(false));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void testGet() {
        FList.empty().get(0);
    }

    @Test
    public void testGet1() {
        assertThat(FList.of(1).get(0), is(1));
    }

    @Test
    public void testGet2() {
        assertThat(FList.of(9, 2).get(1), is(2));
    }

    @Test
    public void testAdd() {
        FList<Number> cdr = FList.of(3.14);
        FList<Number> actual = cdr.add(1);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is(1));
        assertThat(actual.get(1), is(3.14));
    }

    @Test
    public void testEmpty() {
        assertThat(empty(), is(sameInstance(empty())));
    }

    @Test
    public void testCons() {
        FList<Number> cdr = FList.of(3.14);
        FList<Number> actual = cons(1, cdr);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is(1));
        assertThat(actual.get(1), is(3.14));
    }

    @Test
    public void testAppend() {
        FList<Integer> actual = append(FList.empty(), FList.of(1));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(1));
    }

    @Test
    public void testAppend1() {
        FList<Number> actual = append(FList.of(1), FList.<Number>of(3.14));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is(1));
        assertThat(actual.get(1), is(3.14));
    }

    @Test
    public void testOf() {
        FList<Number> actual = FList.of(1, 3.14);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is(1));
        assertThat(actual.get(1), is(3.14));
    }
}
