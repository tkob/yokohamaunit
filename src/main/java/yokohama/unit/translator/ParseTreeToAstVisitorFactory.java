package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.Optional;

public class ParseTreeToAstVisitorFactory {
    public ParseTreeToAstVisitor create(Optional<Path> docyPath) {
        return new ParseTreeToAstVisitor(docyPath);
    }
}
