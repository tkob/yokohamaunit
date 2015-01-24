package yokohama.unit.ast_junit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ImportedNameTest {
    
    @Test
    public void testCompareTo() {
        ImportedName that = new ImportClass("a");
        ImportedName instance = new ImportStatic("a");
        int actual = instance.compareTo(that);
        assertThat(actual, is(0));
    }

    @Test
    public void testCompareTo1() {
        ImportedName that = new ImportClass("b");
        ImportedName instance = new ImportStatic("a");
        int actual = instance.compareTo(that);
        assertThat(actual, lessThan(0));
    }

    @Test
    public void testCompareTo2() {
        ImportedName that = new ImportStatic("a");
        ImportedName instance = new ImportClass("b");
        int actual = instance.compareTo(that);
        assertThat(actual, greaterThan(0));
    }
}
