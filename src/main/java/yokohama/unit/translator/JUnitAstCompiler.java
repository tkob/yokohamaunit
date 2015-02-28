package yokohama.unit.translator;

import java.util.List;
import yokohama.unit.ast_junit.CompilationUnit;

public interface JUnitAstCompiler {
    boolean compile(
            CompilationUnit ast,
            String className,
            String packageName,
            List<String> javacArgs
    );
}
