package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface DocyCompiler {
    boolean compile(
            Path docyPath,
            InputStream ins,
            String className,
            String packageName,
            List<String> javacArgs
    ) throws IOException ;
}
