package yokohama.unit.util;

import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SBuilderTest {
    
    /**
     * Test of appendln method, of class SBuilder.
     */
    @Test
    public void testAppendln() {
        SBuilder instance = new SBuilder(4);
        instance.appendln();
        String actual = instance.toString();

        StrBuilder expected = new StrBuilder();
        expected.appendNewLine();

        assertThat(instance.toString(), is(expected.toString()));
    }

    /**
     * Test of appendln method, of class SBuilder.
     */
    @Test
    public void testAppendln1() {
        SBuilder instance = new SBuilder(4);
        instance.appendln("a");
        String actual = instance.toString();

        StrBuilder expected = new StrBuilder();
        expected.appendln("a");

        assertThat(instance.toString(), is(expected.toString()));
    }

    /**
     * Test of appendln method, of class SBuilder.
     */
    @Test
    public void testAppendln2() {
        SBuilder instance = new SBuilder(4);
        instance.appendln("a", "b");
        String actual = instance.toString();

        StrBuilder expected = new StrBuilder();
        expected.appendln("ab");

        assertThat(instance.toString(), is(expected.toString()));
    }

    /**
     * Test of appendln method, of class SBuilder.
     */
    @Test
    public void testAppendln3() {
        SBuilder instance = new SBuilder(4);
        instance.shift();
        instance.appendln("a", "b");
        String actual = instance.toString();

        StrBuilder expected = new StrBuilder();
        expected.appendln("    ab");

        assertThat(instance.toString(), is(expected.toString()));
    }

    /**
     * Test of shift method, of class SBuilder.
     */
    @Test
    public void testShift() {
        SBuilder instance = new SBuilder(4);
        for (int i = 0; i < 3; i++) {
            instance.shift();
        }
        assertThat(instance.getIndent(), is(4 * 3));
    }

    /**
     * Test of unshift method, of class SBuilder.
     */
    @Test
    public void testUnshift() {
        SBuilder instance = new SBuilder(4);
        instance.unshift();
        assertThat(instance.getIndent(), is(0));
    }

    /**
     * Test of unshift method, of class SBuilder.
     */
    @Test
    public void testUnshift1() {
        SBuilder instance = new SBuilder(4);
        instance.shift();
        instance.unshift();
        assertThat(instance.getIndent(), is(0));
    }

    /**
     * Test of unshift method, of class SBuilder.
     */
    @Test
    public void testUnshift2() {
        SBuilder instance = new SBuilder(4);
        instance.shift();
        instance.shift();
        instance.unshift();
        assertThat(instance.getIndent(), is(4 * 1));
    }

    /**
     * Test of unshift method, of class SBuilder.
     */
    @Test
    public void testUnshift3() {
        SBuilder instance = new SBuilder(4);
        instance.shift();
        instance.shift();
        instance.unshift();
        instance.unshift();
        assertThat(instance.getIndent(), is(0));
    }

}
