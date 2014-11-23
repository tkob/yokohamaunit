package yokohama.unit.grammar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class YokohamaUnitLexerTest {

    public static List<String> stringToTokens(String input)
            throws IOException {
        InputStream bais = new ByteArrayInputStream(input.getBytes());
        CharStream stream = new ANTLRInputStream(bais);
        Lexer lex = new YokohamaUnitLexer(stream);
        return lex.getAllTokens().stream()
                                 .map(Token::getText)
                                 .collect(Collectors.toList());
    }

    @DataPoints
    public static Fixture[] PARAMs = {
        new Fixture("Test: Simple Test\n", Arrays.asList("Test:", "Simple Test")),
        new Fixture("Table Assert", Arrays.asList("Table", "Assert")),
        new Fixture("that and using where", Arrays.asList("that", "and", "using", "where")),
        new Fixture(".=", Arrays.asList(".", "=")),
        new Fixture("is throws", Arrays.asList("is", "throws")),
        new Fixture("table CSV TSV Excel", Arrays.asList("table", "CSV", "TSV", "Excel")),
        new Fixture("`a`", Arrays.asList("a")),
        new Fixture("`a||b`", Arrays.asList("a||b")),
        new Fixture("|a|b\n", Arrays.asList("|", "a", "|", "b", "\n")),
        new Fixture("|a|b\n----\n", Arrays.asList("|", "a", "|", "b", "\n", "----\n")),
        new Fixture("|a|b\n----\n\n==", Arrays.asList("|", "a", "|", "b", "\n", "----\n", "=", "=")),
        new Fixture("|a|b|\n", Arrays.asList("|", "a", "|", "b", "|", "\n")),
        new Fixture("|a|b|\n-----\n", Arrays.asList("|", "a", "|", "b", "|", "\n", "-----\n")),
        new Fixture("|a|b\n-----\n|c|d\n", Arrays.asList("|", "a", "|", "b", "\n", "-----\n", "|", "c", "|", "d", "\n")),
        new Fixture("|a\n|==\n", Arrays.asList("|", "a", "\n", "|", "==", "\n")),
    };

    @Theory
    public void testLexer(final Fixture fixture) throws Exception {
        List<String> expected = fixture.tokens;
        List<String> actual = stringToTokens(fixture.input);
        assertThat(actual, is(expected));
    }

    @AllArgsConstructor
    public static class Fixture {
        public final String input;
        public final List<String> tokens;
    }
}