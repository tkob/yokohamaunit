package yokohama.unit.translator;

import yokohama.unit.ast.TableExtractVisitor;

public class AstToJUnitAstFactory {
    public AstToJUnitAst create(
            String className,
            String packageName,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy) {
        return new AstToJUnitAst(
                className,
                packageName,
                expressionStrategy,
                mockStrategy,
                new TableExtractVisitor());
    }
}
