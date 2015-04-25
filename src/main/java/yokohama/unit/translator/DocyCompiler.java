package yokohama.unit.translator;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import yokohama.unit.position.ErrorMessage;

public interface DocyCompiler {
    List<ErrorMessage> compile(
            Path docyPath,
            InputStream ins,
            String name,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            boolean emitJava,
            List<String> javacArgs
    );
}
