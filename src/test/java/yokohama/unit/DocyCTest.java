package yokohama.unit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import org.junit.Test;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import yokohama.unit.translator.DocyCompiler;

public class DocyCTest {
    
    @Test
    public void testExtractOptions() throws ParseException {
        String[] args = {};
        List<Option> options = Arrays.asList(new BasicParser().parse(DocyC.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList();
        List<String> actual = DocyC.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions1() throws ParseException {
        String[] args = {"-d", ".", "-target", "1.8"};
        List<Option> options = Arrays.asList(new BasicParser().parse(DocyC.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList();
        List<String> actual = DocyC.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions2() throws ParseException {
        String[] args = {};
        List<Option> options = Arrays.asList(new BasicParser().parse(DocyC.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList("d",  "target");
        List<String> actual = DocyC.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    @Test
    public void testExtractOptions3() throws ParseException {
        String[] args = {"-d", ".", "-encoding", "UTF-8", "-nowarn", "-target", "1.8"};
        List<Option> options = Arrays.asList(new BasicParser().parse(DocyC.constructOptions(), args).getOptions());
        List<String> delegatedOptions = Arrays.asList("nowarn", "verbose", "d",  "target");
        List<String> actual = DocyC.extractOptions(options, delegatedOptions);
        List<String> expected = Arrays.asList("-d", ".", "-nowarn", "-target", "1.8");
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun() {
        String[] args = { "-help" };
        DocyCompiler compiler = mock(DocyCompiler.class);
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new DocyC(compiler, fisFactory).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun0() {
        String[] args = {};
        DocyCompiler compiler = mock(DocyCompiler.class);
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new DocyC(compiler, fisFactory).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun1() {
        String[] args = { "-d" };
        DocyCompiler compiler = mock(DocyCompiler.class);
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new DocyC(compiler, fisFactory).run(in, out, err, args);
        int expected = Command.EXIT_FAILURE;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun2() {
        String[] args = { "-xxxxxx" };
        DocyCompiler compiler = mock(DocyCompiler.class);
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new DocyC(compiler, fisFactory).run(in, out, err, args);
        int expected = Command.EXIT_FAILURE;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun3() throws IOException {
        assumeTrue(System.getProperty("os.name").contains("Windows"));
        String[] args = { "-basedir", "E:\\src\\main", "E:\\src\\main\\yokohama\\unit\\Foo.docy"};
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyBoolean(),
                anyBoolean(),
                anyObject()))
                .thenReturn(Collections.emptyList());
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new DocyC(compiler, fisFactory).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(compiler).compile(
                eq(Paths.get("E:/src/main/yokohama/unit/Foo.docy")),
                anyObject(),
                eq("Foo"),
                eq("yokohama.unit"),
                anyObject(),
                anyObject(),
                eq(false),
                eq(false),
                eq(Arrays.asList()));
    }

    @Test
    public void testRun4() throws IOException {
        assumeTrue(System.getProperty("os.name").contains("Windows"));
        String[] args = { "-basedir", "E:\\src\\main\\", "E:\\src\\main\\yokohama\\unit\\Foo.docy"};
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyBoolean(),
                anyBoolean(),
                anyObject()))
                .thenReturn(Collections.emptyList());
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new DocyC(compiler, fisFactory).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(compiler).compile(
                eq(Paths.get("E:/src/main/yokohama/unit/Foo.docy")),
                anyObject(),
                eq("Foo"),
                eq("yokohama.unit"),
                anyObject(),
                anyObject(),
                eq(false),
                eq(false),
                eq(Arrays.asList()));
    }

    @Test
    public void testRun5() throws IOException {
        assumeTrue(System.getProperty("os.name").contains("Linux"));
        String[] args = { "-basedir", "/home/user/src/main", "/home/user/src/main/yokohama/unit/Foo.docy"};
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyBoolean(),
                anyBoolean(),
                anyObject()))
                .thenReturn(Collections.emptyList());
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new DocyC(compiler, fisFactory).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(compiler).compile(
                eq(Paths.get("/home/user/src/main/yokohama/unit/Foo.docy")),
                anyObject(),
                eq("Foo"),
                eq("yokohama.unit"),
                anyObject(),
                anyObject(),
                eq(false),
                eq(false),
                eq(Arrays.asList()));
    }

    @Test
    public void testRun6() throws IOException {
        assumeTrue(System.getProperty("os.name").contains("Linux"));
        String[] args = { "-basedir", "/home/user/src/main/", "/home/user/src/main/yokohama/unit/Foo.docy"};
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyBoolean(),
                anyBoolean(),
                anyObject()))
                .thenReturn(Collections.emptyList());
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        int actual = new DocyC(compiler, fisFactory).run(in, out, err, args);
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(compiler).compile(
                eq(Paths.get("/home/user/src/main/yokohama/unit/Foo.docy")),
                anyObject(),
                eq("Foo"),
                eq("yokohama.unit"),
                anyObject(),
                anyObject(),
                eq(false),
                eq(false),
                eq(Arrays.asList()));
    }

    @Test
    public void testRun7() throws IOException {
        String[] args = { "-j", "Foo.docy" };
        DocyCompiler compiler = mock(DocyCompiler.class);
        when(compiler.compile(
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyBoolean(),
                anyBoolean(),
                anyObject()))
                .thenReturn(Collections.emptyList());
        FileInputStreamFactory fisFactory = mock(FileInputStreamFactory.class);
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);

        int ignored = new DocyC(compiler, fisFactory).run(in, out, err, args);

        verify(compiler).compile(
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                anyObject(),
                eq(true),
                eq(false),
                eq(Arrays.asList()));
    }

}
