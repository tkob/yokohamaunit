package yokohama.unit.translator;

import yokohama.unit.ast.TableExtractVisitor;
import yokohama.unit.util.GenSym;

public class AstToJUnitAstFactory {
    public AstToJUnitAst create(
            String name,
            String packageName,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy,
            GenSym genSym) {
        return new AstToJUnitAst(
                name,
                packageName,
                expressionStrategy,
                mockStrategy,
                genSym,
                new TableExtractVisitor());
    }
}
