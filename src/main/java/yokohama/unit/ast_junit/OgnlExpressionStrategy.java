package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

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
    public void bind(SBuilder sb, Binding binding, MockStrategy mockStrategy) {
        String name = binding.getName();
        binding.getValue().<Void>accept(
                quotedExpr -> {
                    sb.appendln("env.put(\"" + escapeJava(name) + "\", Ognl.getValue(\"", escapeJava(quotedExpr.getText()), "\", env));");
                    return null;
                },
                stubExpr -> {
                    sb.appendln("{");
                    sb.shift();
                    mockStrategy.stub(sb, "stub", stubExpr, this);
                    sb.appendln("env.put(\"" + escapeJava(name) + "\", stub);");
                    sb.unshift();
                    sb.appendln("}");
                    return null;
                }
        );
    }

    @Override
    public Set<ImportedName> bindImports(Binding binding, MockStrategy mockStrategy) {
        return binding.getValue().<Set<ImportedName>>accept(
                quotedExpr ->
                    new TreeSet<>(Arrays.asList(new ImportClass("ognl.Ognl"))),
                stubExpr -> mockStrategy.stubImports(stubExpr, this)
        );
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