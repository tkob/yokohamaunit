package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

public class OgnlExpressionStrategy implements ExpressionStrategy {

    @Override
    public String environment() {
        return "OgnlContext env = new OgnlContext();";
    }

    @Override
    public Set<ImportedName> environmentImports() {
        return new TreeSet<>(Arrays.asList(new ImportClass("ognl.OgnlContext")));
    }

    @Override
    public String bind(Binding binding) {
        String name = binding.getName();
        String value = binding.getValue();
        return "env.put(\"" + escapeJava(name) + "\", Ognl.getValue(\"" + escapeJava(value) + "\", env));";
    }

    @Override
    public Set<ImportedName> bindImports() {
        return new TreeSet<>(Arrays.asList(new ImportClass("ognl.Ognl")));
    }

    @Override
    public String getValue(String expression) {
        return "Ognl.getValue(\"" + escapeJava(expression) + "\", env)";
    }

    @Override
    public Set<ImportedName> getValueImports() {
        return new TreeSet<>(Arrays.asList(new ImportClass("ognl.Ognl")));
    }

    @Override
    public Optional<String> wrappingException() {
        return Optional.of("OgnlException");
    }

    @Override
    public Set<ImportedName> wrappingExceptionImports() {
        return new TreeSet<>(Arrays.asList(new ImportClass("ognl.OgnlException")));
    }

    @Override
    public String wrappedException(String e) {
        return e + ".getReason()";
    }

    @Override
    public Set<ImportedName> wrappedExceptionImports() {
        return new TreeSet<>();
    }
}
