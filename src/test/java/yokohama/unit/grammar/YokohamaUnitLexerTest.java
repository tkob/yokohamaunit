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
        new Fixture("# Test: Simple Test\n", Arrays.asList("# Test: ", "Simple Test")),
        new Fixture("[Test Fixture]\n", Arrays.asList("[", "Test Fixture", "]")),
        new Fixture("## Setup \t\nLet", Arrays.asList("## Setup", "Let")),
        new Fixture("## Setup: make a mock\nLet", Arrays.asList("## Setup", "make a mock", "Let")),
        new Fixture("## Exercise \t\nDo", Arrays.asList("## Exercise", "Do")),
        new Fixture("## Exercise: call xxx method\nDo", Arrays.asList("## Exercise", "call xxx method", "Do")),
        new Fixture("## Verify \t\nAssert", Arrays.asList("## Verify", "Assert")),
        new Fixture("## Verify: get result\nAssert", Arrays.asList("## Verify", "get result", "Assert")),
        new Fixture("## Teardown \t\nDo", Arrays.asList("## Teardown", "Do")),
        new Fixture("## Teardown: clean directory\nDo", Arrays.asList("## Teardown", "clean directory", "Do")),
        new Fixture("Assert", Arrays.asList("Assert")),
        new Fixture("that and using where be", Arrays.asList("that", "and", "using", "where", "be")),
        new Fixture(".=", Arrays.asList(".", "=")),
        new Fixture("is throws", Arrays.asList("is", "throws")),
        new Fixture("Table [a]", Arrays.asList("Table",  "[", "a", "]")),
        new Fixture("Table [a'b]", Arrays.asList("Table", "[", "a'b", "]")),
        new Fixture("CSV 'a'", Arrays.asList("CSV '",  "a", "'")),
        new Fixture("CSV 'a''b'", Arrays.asList("CSV '",  "a''b", "'")),
        new Fixture("TSV 'a'", Arrays.asList("TSV '",  "a", "'")),
        new Fixture("TSV 'a''b'", Arrays.asList("TSV '",  "a''b", "'")),
        new Fixture("Excel 'a'", Arrays.asList("Excel '",  "a", "'")),
        new Fixture("Excel 'a''b'", Arrays.asList("Excel '",  "a''b", "'")),
        new Fixture("`a`", Arrays.asList("`", "a", "`")),
        new Fixture("`a||b`", Arrays.asList("`", "a||b", "`")),
        new Fixture("|a|b|\n", Arrays.asList("|", "a", "|", "b", "|\n")),
        new Fixture("|a|b| \n", Arrays.asList("|", "a", "|", "b", "| \n")),
        new Fixture("| ---- |\n", Arrays.asList("| ---- |\n")),
        new Fixture("|-=:.+ |\n", Arrays.asList("|-=:.+ |\n")),
        new Fixture("| ---- | \n\n==", Arrays.asList("| ---- | \n", "=", "=")),
        new Fixture("|==|\n", Arrays.asList("|==|\n")),
        new Fixture("an invocation of `java.util.Option.of()`", Arrays.asList("an invocation of `", "java", ".", "util", ".", "Option", ".", "of", "(", ")", "`")),
        new Fixture("0", Arrays.asList("0")),
        new Fixture("2147483647", Arrays.asList("2147483647")),
        new Fixture("2147483648", Arrays.asList("2147483648")),
        new Fixture("0x7fff_ffff", Arrays.asList("0x7fff_ffff")),
        new Fixture("0177_7777_7777", Arrays.asList("0177_7777_7777")),
        new Fixture("0b0111_1111_1111_1111_1111_1111_1111_1111", Arrays.asList("0b0111_1111_1111_1111_1111_1111_1111_1111")),
        new Fixture("0x8000_0000", Arrays.asList("0x8000_0000")),
        new Fixture("0200_0000_0000", Arrays.asList("0200_0000_0000")),
        new Fixture("0b1000_0000_0000_0000_0000_0000_0000_0000", Arrays.asList("0b1000_0000_0000_0000_0000_0000_0000_0000")),
        new Fixture("0xffff_ffff", Arrays.asList("0xffff_ffff")),
        new Fixture("0377_7777_7777", Arrays.asList("0377_7777_7777")),
        new Fixture("0b1111_1111_1111_1111_1111_1111_1111_1111", Arrays.asList("0b1111_1111_1111_1111_1111_1111_1111_1111")),
        new Fixture("0x_f", Arrays.asList("0", "x_f")),
        new Fixture("0b_0", Arrays.asList("0", "b_0")),
        new Fixture("0_7", Arrays.asList("0_7")),
        new Fixture("0.", Arrays.asList("0", ".")),
        // DecimalFloatingPointLiteral 
        new Fixture("0.0", Arrays.asList("0.0")),
        new Fixture("0.e1", Arrays.asList("0.e1")),
        new Fixture("0.e+1", Arrays.asList("0.e+1")),
        new Fixture("0.e-1", Arrays.asList("0.e-1")),
        new Fixture("0.0f", Arrays.asList("0.0f")),
        new Fixture("0.e1f", Arrays.asList("0.e1f")),
        new Fixture("0.e+1f", Arrays.asList("0.e+1f")),
        new Fixture("0.e-1f", Arrays.asList("0.e-1f")),
        new Fixture("0.0d", Arrays.asList("0.0d")),
        new Fixture("0.e1d", Arrays.asList("0.e1d")),
        new Fixture("0.e+1d", Arrays.asList("0.e+1d")),
        new Fixture("0.e-1d", Arrays.asList("0.e-1d")),
        new Fixture(".0", Arrays.asList(".0")),
        new Fixture(".0f", Arrays.asList(".0f")),
        new Fixture(".0d", Arrays.asList(".0d")),
        new Fixture("0e1", Arrays.asList("0e1")),
        new Fixture("0e+1", Arrays.asList("0e+1")),
        new Fixture("0e-1", Arrays.asList("0e-1")),
        new Fixture("0f", Arrays.asList("0f")),
        new Fixture("0e1f", Arrays.asList("0e1f")),
        new Fixture("0e+1f", Arrays.asList("0e+1f")),
        new Fixture("0e-1f", Arrays.asList("0e-1f")),
        new Fixture("0d", Arrays.asList("0d")),
        new Fixture("0e1d", Arrays.asList("0e1d")),
        new Fixture("0e+1d", Arrays.asList("0e+1d")),
        new Fixture("0e-1d", Arrays.asList("0e-1d")),
        // HexadecimalFloatingPointLiteral
        new Fixture("0x0p1", Arrays.asList("0x0p1")),
        new Fixture("0x0p+1", Arrays.asList("0x0p+1")),
        new Fixture("0x0p-1", Arrays.asList("0x0p-1")),
        new Fixture("0x0.p1", Arrays.asList("0x0.p1")),
        new Fixture("0x0.p+1", Arrays.asList("0x0.p+1")),
        new Fixture("0x0.p-1", Arrays.asList("0x0.p-1")),
        new Fixture("0x0.0p1", Arrays.asList("0x0.0p1")),
        new Fixture("0x0.0p+1", Arrays.asList("0x0.0p+1")),
        new Fixture("0x0.0p-1", Arrays.asList("0x0.0p-1")),
        new Fixture("0x0p1f", Arrays.asList("0x0p1f")),
        new Fixture("0x0p+1f", Arrays.asList("0x0p+1f")),
        new Fixture("0x0p-1f", Arrays.asList("0x0p-1f")),
        new Fixture("0x0.p1f", Arrays.asList("0x0.p1f")),
        new Fixture("0x0.p+1f", Arrays.asList("0x0.p+1f")),
        new Fixture("0x0.p-1f", Arrays.asList("0x0.p-1f")),
        new Fixture("0x0.0p1f", Arrays.asList("0x0.0p1f")),
        new Fixture("0x0.0p+1f", Arrays.asList("0x0.0p+1f")),
        new Fixture("0x0.0p-1f", Arrays.asList("0x0.0p-1f")),
        new Fixture("0x0p1d", Arrays.asList("0x0p1d")),
        new Fixture("0x0p+1d", Arrays.asList("0x0p+1d")),
        new Fixture("0x0p-1d", Arrays.asList("0x0p-1d")),
        new Fixture("0x0.p1d", Arrays.asList("0x0.p1d")),
        new Fixture("0x0.p+1d", Arrays.asList("0x0.p+1d")),
        new Fixture("0x0.p-1d", Arrays.asList("0x0.p-1d")),
        new Fixture("0x0.0p1d", Arrays.asList("0x0.0p1d")),
        new Fixture("0x0.0p+1d", Arrays.asList("0x0.0p+1d")),
        new Fixture("0x0.0p-1d", Arrays.asList("0x0.0p-1d")),
        // Character
        new Fixture("'\\''", Arrays.asList("'", "\\'", "'")),
        new Fixture("'a'", Arrays.asList("'", "a", "'")),
        // String
        new Fixture("\"\\\"\"", Arrays.asList("\"", "\\\"", "\"")),
        new Fixture("\"abc\"", Arrays.asList("\"", "abc", "\"")),
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