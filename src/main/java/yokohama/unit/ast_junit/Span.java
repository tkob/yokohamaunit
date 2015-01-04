package yokohama.unit.ast_junit;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Value;
import yokohama.unit.ast.Position;

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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sourcePath.isPresent() ? sourcePath.get().toString() : "?");
        sb.append(":");
        if (start.getLine() < 0) {
            sb.append("?");
        } else {
            sb.append(start.getLine());
            if (start.getColumn() >= 0) {
                sb.append(".");
                sb.append(start.getColumn());
            }
            if (end.getLine() >= 0) {
                sb.append("-");
                sb.append(end.getLine());
                if (end.getColumn() >= 0) {
                    sb.append(".");
                    sb.append(end.getColumn());
                }
            }
        }
        return sb.toString();
    }

    private static Span dummySpan =
            new Span(
                    Optional.empty(),
                    new Position(-1, -1),
                    new Position(-1, -1));
    public static Span dummySpan() {
        return dummySpan;
    }
}
