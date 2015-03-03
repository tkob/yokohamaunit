package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.Optional;

public class AstToJUnitAstFactory {
    public AstToJUnitAst create(
            Optional<Path> docyPath,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy) {
        return new AstToJUnitAst(docyPath, expressionStrategy, mockStrategy);
    }
}
