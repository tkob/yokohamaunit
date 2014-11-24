package yokohama.unit;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class MainTest {
    
    @Test
    public void testExtractOptions() throws ParseException {
        String[] args = {};
        List<Option> options = Arrays.asList(new BasicParser().parse(Main.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList();
        List<String> actual = Main.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions1() throws ParseException {
        String[] args = {"-d", ".", "-target", "1.8"};
        List<Option> options = Arrays.asList(new BasicParser().parse(Main.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList();
        List<String> actual = Main.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions2() throws ParseException {
        String[] args = {};
        List<Option> options = Arrays.asList(new BasicParser().parse(Main.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList("d",  "target");
        List<String> actual = Main.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions3() throws ParseException {
        String[] args = {"-d", ".", "-encoding", "UTF-8", "-nowarn", "-target", "1.8"};
        List<Option> options = Arrays.asList(new BasicParser().parse(Main.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList("nowarn", "verbose", "d",  "target");
        List<String> actual = Main.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList("-d", ".", "-nowarn", "-target", "1.8");
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun() {
        String[] args = { "-help" };
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new Main().run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun0() {
        String[] args = {};
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new Main().run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun1() {
        String[] args = { "-d" };
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new Main().run(in, out, err, args);
        int expected = Command.EXIT_FAILURE;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun2() {
        String[] args = { "-xxxxxx" };
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new Main().run(in, out, err, args);
        int expected = Command.EXIT_FAILURE;
        assertThat(actual, is(expected));
    }

}
