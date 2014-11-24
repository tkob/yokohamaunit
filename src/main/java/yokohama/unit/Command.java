package yokohama.unit;

import java.io.InputStream;
import java.io.PrintStream;

public interface Command {
    int EXIT_SUCCESS = 0;
    int EXIT_FAILURE = 1;

    int run(InputStream in, PrintStream out, PrintStream err, String... args);
}
