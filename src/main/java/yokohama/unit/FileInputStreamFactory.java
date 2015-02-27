package yokohama.unit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInputStreamFactory {
    public InputStream create(File file) throws IOException {
        return new FileInputStream(file);
    }
}
