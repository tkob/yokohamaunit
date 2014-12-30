package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class CompilationUnit {
    private String packageName;
    private ClassDecl classDecl;

    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        return classDecl.importedNames(expressionStrategy, mockStrategy);
    }

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
        for (ImportedName name : importedNames(expressionStrategy, mockStrategy)) {
            name.toString(sb);
        }
        sb.appendln();
        classDecl.toString(sb, expressionStrategy, mockStrategy);
    }
}
