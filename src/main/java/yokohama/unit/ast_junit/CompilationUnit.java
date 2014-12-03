package yokohama.unit.ast_junit;

import java.util.Set;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class CompilationUnit {
    private String packageName;
    private ClassDecl classDecl;

    public Set<ImportedName> importedNames(ExpressionStrategy expressionStrategy) {
        return classDecl.importedNames(expressionStrategy);
    }

    public String getText(ExpressionStrategy expressionStrategy) {
        final SBuilder sb = new SBuilder(4);
        toString(sb, expressionStrategy);
        return sb.toString();
    }

    public void toString(SBuilder sb, ExpressionStrategy expressionStrategy) {
        sb.appendln("package ", packageName, ";");
        sb.appendln();
        for (ImportedName name : importedNames(expressionStrategy)) {
            name.toString(sb);
        }
        sb.appendln();
        classDecl.toString(sb, expressionStrategy);
    }
}
