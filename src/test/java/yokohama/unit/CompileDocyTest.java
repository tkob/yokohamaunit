package yokohama.unit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import org.junit.Test;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import yokohama.unit.translator.DocyCompiler;

public class CompileDocyTest {
    
    @Test
    public void testExtractOptions() throws ParseException {
        String[] args = {};
        List<Option> options = Arrays.asList(new BasicParser().parse(CompileDocy.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList();
        List<String> actual = CompileDocy.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions1() throws ParseException {
        String[] args = {"-d", ".", "-target", "1.8"};
        List<Option> options = Arrays.asList(new BasicParser().parse(CompileDocy.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList();
        List<String> actual = CompileDocy.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions2() throws ParseException {
        String[] args = {};
        List<Option> options = Arrays.asList(new BasicParser().parse(CompileDocy.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList("d",  "target");
        List<String> actual = CompileDocy.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions3() throws ParseException {
        String[] args = {"-d", ".", "-encoding", "UTF-8", "-nowarn", "-target", "1.8"};
        List<Option> options = Arrays.asList(new BasicParser().parse(CompileDocy.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList("nowarn", "verbose", "d",  "target");
        List<String> actual = CompileDocy.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList("-d", ".", "-nowarn", "-target", "1.8");
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun() {
        String[] args = { "-help" };
        DocyCompiler compiler = mock(DocyCompiler.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new CompileDocy(compiler).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun0() {
        String[] args = {};
        DocyCompiler compiler = mock(DocyCompiler.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new CompileDocy(compiler).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun1() {
        String[] args = { "-d" };
        DocyCompiler compiler = mock(DocyCompiler.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new CompileDocy(compiler).run(in, out, err, args);
        int expected = Command.EXIT_FAILURE;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun2() {
        String[] args = { "-xxxxxx" };
        DocyCompiler compiler = mock(DocyCompiler.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new CompileDocy(compiler).run(in, out, err, args);
        int expected = Command.EXIT_FAILURE;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun3() throws IOException {
        assumeTrue(System.getProperty("os.name").contains("Windows"));
        String[] args = { "-basedir", "E:\\src\\main", "E:\\src\\main\\yokohama\\unit\\Foo.docy"};
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(anyObject(), anyObject(), anyObject(), anyObject()))
                .thenReturn(true);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new CompileDocy(compiler).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(compiler).compile(URI.create("file:///E:/src/main/yokohama/unit/Foo.docy"), "Foo", "yokohama.unit", Arrays.asList());
    }

    @Test
    public void testRun4() throws IOException {
        assumeTrue(System.getProperty("os.name").contains("Windows"));
        String[] args = { "-basedir", "E:\\src\\main\\", "E:\\src\\main\\yokohama\\unit\\Foo.docy"};
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(anyObject(), anyObject(), anyObject(), anyObject()))
                .thenReturn(true);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new CompileDocy(compiler).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(compiler).compile(URI.create("file:///E:/src/main/yokohama/unit/Foo.docy"), "Foo", "yokohama.unit", Arrays.asList());
    }

    @Test
    public void testRun5() throws IOException {
        assumeTrue(System.getProperty("os.name").contains("Linux"));
        String[] args = { "-basedir", "/home/user/src/main", "/home/user/src/main/yokohama/unit/Foo.docy"};
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(anyObject(), anyObject(), anyObject(), anyObject()))
                .thenReturn(true);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new CompileDocy(compiler).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(compiler).compile(URI.create("file:///home/user/src/main/yokohama/unit/Foo.docy"), "Foo", "yokohama.unit", Arrays.asList());
    }

    @Test
    public void testRun6() throws IOException {
        assumeTrue(System.getProperty("os.name").contains("Linux"));
        String[] args = { "-basedir", "/home/user/src/main/", "/home/user/src/main/yokohama/unit/Foo.docy"};
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(anyObject(), anyObject(), anyObject(), anyObject()))
                .thenReturn(true);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new CompileDocy(compiler).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(compiler).compile(URI.create("file:///home/user/src/main/yokohama/unit/Foo.docy"), "Foo", "yokohama.unit", Arrays.asList());
    }

}