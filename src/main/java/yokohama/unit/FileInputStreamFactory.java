package yokohama.unit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FileInputStreamFactory {
    public InputStream create(Path path) throws IOException {
        return new FileInputStream(path.toFile());
    }
}
