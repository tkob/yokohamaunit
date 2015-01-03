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
}
