package yokohama.unit.ast_junit;

import java.util.Optional;
import java.util.Set;

public interface ExpressionStrategy {
    public String environment();
    public String bind(Binding binding);
    public String getValue(String expression);
    public Optional<String> wrappingException();
    public String wrappedException(String e);
    
    public Set<ImportedName> environmentImports();
    public Set<ImportedName> bindImports();
    public Set<ImportedName> getValueImports();
    public Set<ImportedName> wrappingExceptionImports();
    public Set<ImportedName> wrappedExceptionImports();
}
