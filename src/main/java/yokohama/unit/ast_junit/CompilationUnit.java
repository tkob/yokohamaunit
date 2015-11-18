package yokohama.unit.ast_junit;

import java.util.List;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class CompilationUnit {
    private String packageName;
    private List<ClassDecl> classDecls;

    public String getText() {
        final SBuilder sb = new SBuilder(4);
        toString(sb);
        return sb.toString();
    }

    public void toString(SBuilder sb) {
        sb.appendln("package ", packageName, ";");
        sb.appendln();
        for (ClassDecl classDecl : classDecls) {
            classDecl.toString(sb);
        }
    }
}
