package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class CompilationUnit {
    private String packageName;
    private ClassDecl classDecl;

    public String getText(
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy
    ) {
        final SBuilder sb = new SBuilder(4);
        toString(sb, expressionStrategy, mockStrategy);
        return sb.toString();
    }

    public void toString(
            SBuilder sb,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy
    ) {
        sb.appendln("package ", packageName, ";");
        sb.appendln();
        classDecl.toString(sb, expressionStrategy, mockStrategy);
    }
}
