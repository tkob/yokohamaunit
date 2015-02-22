package yokohama.unit.translator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;

public class DocyCompilerImpl implements DocyCompiler {
    @Override
    public boolean compile(
            Path docyPath,
            String className,
            String packageName,
            List<String> javacArgs
    ) throws IOException {
        return TranslatorUtils.compileDocy(
                Optional.of(docyPath),
                IOUtils.toString(docyPath.toUri()),
                className,
                packageName,
                javacArgs.toArray(new String[javacArgs.size()]));
    }
}
