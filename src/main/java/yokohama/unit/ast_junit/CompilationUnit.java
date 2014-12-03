package yokohama.unit.ast_junit;

import java.util.Set;
import java.util.TreeSet;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class CompilationUnit {
    private String packageName;
    private ClassDecl classDecl;

    public Set<ImportedName> importedNames() {
        return classDecl.importedNames()
                .stream()
                .collect(
                        () -> new TreeSet<ImportedName>(),
                        (set, e) -> set.add(e),
                        (s1, s2) -> s1.addAll(s2)
                );
    }

    public String getText() {
        final SBuilder sb = new SBuilder(4);
        toString(sb);
        return sb.toString();
    }

    public void toString(SBuilder sb) {
        sb.appendln("package ", packageName, ";");
        sb.appendln();
        for (ImportedName name : importedNames()) {
            name.toString(sb);
        }
        sb.appendln();
        classDecl.toString(sb);
    }
}
