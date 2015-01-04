package yokohama.unit.ast_junit;

import java.nio.file.Paths;
import java.util.Optional;
import lombok.AllArgsConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import yokohama.unit.ast.Position;

public class SpanTest {
    @RunWith(Theories.class)
    public static class ToString {
        /*
        sourcePath: Optional.empty(), Optional.of(Paths.get("dummy"))
        startLine: -1, 1
        startColumn: -1, 0
        endLine: -1, 1
        endColumn: -1, 0
        IF [startLine] = -1 THEN [startColumn] = -1;
        IF [startLine] = -1 THEN [endLine] = -1;
        IF [startLine] = -1 THEN [endColumn] = -1;
        IF [endLine] = -1 THEN [endColumn] = -1;
         */
        @DataPoints
        public static Fixture[] PARAMs = {
            new Fixture(new Span(Optional.empty(), new Position(1, 0), new Position(-1, -1)), "?:1.0"),
            new Fixture(new Span(Optional.of(Paths.get("dummy")), new Position(1, -1), new Position(1, 0)), "dummy:1-1.0"),
            new Fixture(new Span(Optional.of(Paths.get("dummy")), new Position(1, 0), new Position(1, -1)), "dummy:1.0-1"),
            new Fixture(new Span(Optional.empty(), new Position(1, -1), new Position(1, 0)), "?:1-1.0"),
            new Fixture(new Span(Optional.of(Paths.get("dummy")), new Position(1, 0), new Position(1, 0)), "dummy:1.0-1.0"),
            new Fixture(new Span(Optional.of(Paths.get("dummy")), new Position(-1, -1), new Position(-1, -1)), "dummy:?"),
            new Fixture(new Span(Optional.empty(), new Position(-1, -1), new Position(-1, -1)), "?:?"),
        };
        @Theory
        public void testToString(final Fixture fixture) {
            Span instance = fixture.span;
            String actual = instance.toString();
            String expected = fixture.string;
            assertThat(actual, is(expected));
        }
        @AllArgsConstructor
        public static class Fixture {
            public Span span;
            public String string;
        }
    }
}
