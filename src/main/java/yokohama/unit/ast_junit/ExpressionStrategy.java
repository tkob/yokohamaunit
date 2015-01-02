package yokohama.unit.ast_junit;

import java.util.Optional;
import java.util.Set;
import yokohama.unit.util.SBuilder;

public interface ExpressionStrategy {
    public void auxMethods(SBuilder sb);
    public String environment();
    public void bind(SBuilder sb, Binding binding, MockStrategy mockStrategy);
    public String getValue(String expression);
    public Optional<String> wrappingException();
    public String wrappedException(String e);
    
    public Set<ImportedName> auxMethodsImports();
    public Set<ImportedName> environmentImports();
    public Set<ImportedName> bindImports(Binding binding, MockStrategy mockStrategy);
    public Set<ImportedName> getValueImports();
    public Set<ImportedName> wrappingExceptionImports();
    public Set<ImportedName> wrappedExceptionImports();
}
