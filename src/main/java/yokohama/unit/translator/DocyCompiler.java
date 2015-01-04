package yokohama.unit.translator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DocyCompiler {
    boolean compile(
            Path docy,
            String className,
            String packageName,
            List<String> javacArgs
    ) throws IOException ;
}
