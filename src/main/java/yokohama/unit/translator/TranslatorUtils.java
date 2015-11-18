package yokohama.unit.translator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class TranslatorUtils {
    public static Path makeClassFilePath(
            Optional<Path> dest,
            String packageName,
            String className,
            String ext) {
        Path classFile = (dest.isPresent() ? dest.get(): Paths.get("."))
                .resolve(Paths.get(packageName.replace('.', '/')))
                .resolve(className + ext);
        return classFile;
    }
}
