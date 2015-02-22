package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.Optional;

public class AstToJUnitAstFactory {
    public AstToJUnitAst create(Optional<Path> docyPath) {
        return new AstToJUnitAst(docyPath);
    }
}
