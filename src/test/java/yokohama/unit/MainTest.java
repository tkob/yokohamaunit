package yokohama.unit;

import java.io.InputStream;
import java.io.PrintStream;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MainTest {
    
    @Test
    public void testRun() {
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        CommandFactory factory = new CommandFactory();
        int actual = new Main(factory).run(in, out, err);
        int expected = Command.EXIT_FAILURE;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun1() {
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        CommandFactory factory = new CommandFactory();
        int actual = new Main(factory).run(in, out, err, "unknown");
        int expected = Command.EXIT_FAILURE;
        assertThat(actual, is(expected));
    }

    @Test
    public void testRun2() {
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        Command command = mock(Command.class);
        when(command.run(anyObject(), anyObject(), anyObject(), anyObject()))
                .thenReturn(Command.EXIT_SUCCESS);
        CommandFactory factory = mock(CommandFactory.class);
        when(factory.create("docyc")).thenReturn(command);
        int actual = new Main(factory).run(in, out, err, "docyc");
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(factory).create("docyc");
        verify(command).run(anyObject(), anyObject(), anyObject());
    }

    @Test
    public void testRun3() {
        InputStream in = mock(InputStream.class);
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        Command command = mock(Command.class);
        when(command.run(anyObject(), anyObject(), anyObject(), anyObject()))
                .thenReturn(Command.EXIT_SUCCESS);
        CommandFactory factory = mock(CommandFactory.class);
        when(factory.create("docyc")).thenReturn(command);
        int actual = new Main(factory).run(in, out, err, "docyc", "help");
        int expected = Command.EXIT_SUCCESS;
        assertThat(actual, is(expected));
        verify(factory).create("docyc");
        verify(command).run(anyObject(), anyObject(), anyObject(), eq("help"));
    }

}
