package yokohama.unit.util;

import lombok.AllArgsConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

public class SUtilsTest {
    @RunWith(Theories.class)
    public static class ToIdent {
        @DataPoints
        public static Fixture[] PARAMs = {
            new Fixture("", ""),
            new Fixture("ident", "ident"),
            new Fixture("123", "_123"),
            new Fixture("a,b,,c,", "a_b_c"),
            new Fixture("3.14", "_3_14"),
            new Fixture("__", "__"),
            new Fixture("if", "if_"),
            new Fixture("int", "int_"),
            new Fixture("true", "true_"),
        };

        @Theory
        public void testToIdent(final Fixture fixture) {
            String text = fixture.before;
            String actual = SUtils.toIdent(text);
            String expected = fixture.after;
            assertThat(actual, is(expected));
        }

        @AllArgsConstructor
        public static class Fixture {
            public final String before;
            public final String after;
        }
    }
    
}
