package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.Optional;
import yokohama.unit.ast.TableExtractVisitor;

public class AstToJUnitAstFactory {
    public AstToJUnitAst create(
            Optional<Path> docyPath,
            String className,
            String packageName,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy) {
        return new AstToJUnitAst(
                docyPath,
                className,
                packageName,
                expressionStrategy,
                mockStrategy,
                new TableExtractVisitor());
    }
}
