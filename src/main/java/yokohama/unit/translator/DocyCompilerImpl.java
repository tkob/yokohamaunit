package yokohama.unit.translator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;

public class DocyCompilerImpl implements DocyCompiler {
    @Override
    public boolean compile(
            Path docy,
            String className,
            String packageName,
            List<String> javacArgs
    ) throws IOException {
        return TranslatorUtils.compileDocy(
                IOUtils.toString(docy.toUri()),
                className,
                packageName,
                javacArgs.toArray(new String[javacArgs.size()]));
    }
}
