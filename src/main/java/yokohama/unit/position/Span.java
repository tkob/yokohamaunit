package yokohama.unit.position;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Value;

@Value
public class Span {
    private final Optional<Path> sourcePath;
    private final Position start;
    private final Position end;

    public String getFileName() {
        if (!sourcePath.isPresent()) return "?";

        Path path = sourcePath.get();
        int nameCount = path.getNameCount();

        if (nameCount == 0) return "?";

        String fileName = path.getName(nameCount -1).toString();

        return fileName.equals("") ? "?" : fileName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sourcePath.isPresent() ? sourcePath.get().toString() : "?");
        sb.append(":");
        sb.append(start.toString());
        if (!end.isDummy()) {
            sb.append("-");
            sb.append(end.toString());
        }
        return sb.toString();
    }

    public Span of(Path path) {
        return new Span(Optional.of(path), Position.dummyPos(), Position.dummyPos());
    }

    public Span of(Path path, Position start) {
        return new Span(Optional.of(path), start, Position.dummyPos());
    }

    public Span of(Path path, Position start, Position end) {
        return new Span(Optional.of(path), start, end);
    }

    private static Span dummySpan =
            new Span(
                    Optional.empty(),
                    Position.dummyPos(),
                    Position.dummyPos());
    public static Span dummySpan() {
        return dummySpan;
    }
}
