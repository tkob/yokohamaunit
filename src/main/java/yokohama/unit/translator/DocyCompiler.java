package yokohama.unit.translator;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface DocyCompiler {
    boolean compile(
            URI docy,
            String className,
            String packageName,
            List<String> javacArgs
    ) throws IOException ;
}
