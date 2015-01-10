package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

public class OgnlExpressionStrategy implements ExpressionStrategy {
    @Override
    public void auxMethods(SBuilder sb) {
        sb.appendln("private Object eval(String expression, OgnlContext env, String fileName, int startLine, String span) throws OgnlException {");
        sb.shift();
            sb.appendln("try {");
            sb.shift();
                sb.appendln("return Ognl.getValue(expression, env);");
            sb.unshift();
            sb.appendln("} catch (OgnlException e) {");
            sb.shift();
                sb.appendln("Throwable reason = e.getReason();");
                sb.appendln("OgnlException e2 = reason == null ? new OgnlException(span + \" \" + e.getMessage(), e)",
                                                             " : new OgnlException(span + \" \" + reason.getMessage(), reason);");
                sb.appendln("StackTraceElement[] st = { new StackTraceElement(\"\", \"\", fileName, startLine) };");
                sb.appendln("e2.setStackTrace(st);");
                sb.appendln("throw e2;");
            sb.unshift();
            sb.appendln("}");
        sb.unshift();
        sb.appendln("}");
    }

    @Override
    public Set<ImportedName> auxMethodsImports() {
        return new TreeSet<>(Arrays.asList(
                new ImportClass("ognl.Ognl"),
                new ImportClass("ognl.OgnlContext"),
                new ImportClass("ognl.OgnlException")
        ));
    }

    @Override
    public String environment() {
        return "OgnlContext env = new OgnlContext();";
    }

    @Override
    public Set<ImportedName> environmentImports() {
        return new TreeSet<>(Arrays.asList(new ImportClass("ognl.OgnlContext")));
    }

    @Override
    public void bind(SBuilder sb, TopBinding binding, MockStrategy mockStrategy) {
        String name = binding.getName();
        binding.getValue().<Void>accept(
                quotedExpr -> {
                    sb.appendln(
                            "env.put(\"", escapeJava(name), "\", ",
                            "eval(\"", escapeJava(quotedExpr.getText()), "\", env, ",
                            "\"", escapeJava(quotedExpr.getSpan().getFileName()),  "\", ",
                            quotedExpr.getSpan().getStart().getLine(), ", ",
                            "\"", quotedExpr.getSpan().toString(), "\"));"
                    );
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
                },
                varExpr -> {
                    sb.appendln("env.put(\"", escapeJava(name), "\", ", varExpr.getName(), ");");
                    return null;
                }
        );
    }

    @Override
    public Set<ImportedName> bindImports(TopBinding binding, MockStrategy mockStrategy) {
        return binding.getValue().<Set<ImportedName>>accept(
                quotedExpr ->
                    new TreeSet<>(Arrays.asList(new ImportClass("ognl.Ognl"))),
                stubExpr -> mockStrategy.stubImports(stubExpr, this),
                varExpr -> new TreeSet<>()
        );
    }

    @Override
    public String getValue(QuotedExpr quotedExpr) {
        return "eval(\"" + escapeJava(quotedExpr.getText()) + "\", env, " +
                "\"" + escapeJava(quotedExpr.getSpan().getFileName()) +  "\", " +
                quotedExpr.getSpan().getStart().getLine() + ", " +
                "\"" + quotedExpr.getSpan().toString() + "\")";
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
