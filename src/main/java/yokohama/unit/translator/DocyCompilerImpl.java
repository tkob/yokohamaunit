package yokohama.unit.translator;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class DocyCompilerImpl implements DocyCompiler {
    @Override
    public boolean compile(
            URI docy,
            String className,
            String packageName,
            List<String> javacArgs
    ) throws IOException {
        return TranslatorUtils.compileDocy(
                IOUtils.toString(docy),
                className,
                packageName,
                javacArgs.toArray(new String[javacArgs.size()]));
    }
}
