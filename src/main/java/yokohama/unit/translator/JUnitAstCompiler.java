package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.position.ErrorMessage;

public interface JUnitAstCompiler {
    List<ErrorMessage> compile(
            Path docyPath,
            CompilationUnit ast,
            String className,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            List<String> javacArgs
    );
}
