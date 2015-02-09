package yokohama.unit.ast_junit;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;
import static yokohama.unit.util.SetUtils.setOf;

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
        return setOf(
                new ImportClass("ognl.Ognl"),
                new ImportClass("ognl.OgnlContext"),
                new ImportClass("ognl.OgnlException"));
    }

    @Override
    public String environment() {
        return "OgnlContext env = new OgnlContext();";
    }

    @Override
    public Set<ImportedName> environmentImports() {
        return setOf(new ImportClass("ognl.OgnlContext"));
    }

    @Override
    public void bind(SBuilder sb, String name, Var varExpr) {
        sb.appendln("env.put(\"", escapeJava(name), "\", ", varExpr.getName(), ");");
    }

    @Override
    public Set<ImportedName> bindImports() {
        return new TreeSet<>();
    }

    @Override
    public String getValue(QuotedExpr quotedExpr) {
        return "eval(\"" + escapeJava(quotedExpr.getText()) + "\", env, " +
                "\"" + escapeJava(quotedExpr.getSpan().getFileName()) +  "\", " +
                quotedExpr.getSpan().getStart().getLine() + ", " +
                "\"" + escapeJava(quotedExpr.getSpan().toString()) + "\")";
    }

    @Override
    public Set<ImportedName> getValueImports() {
        return setOf(new ImportClass("ognl.Ognl"));
    }

    @Override
    public Optional<String> wrappingException() {
        return Optional.of("OgnlException");
    }

    @Override
    public Set<ImportedName> wrappingExceptionImports() {
        return setOf(new ImportClass("ognl.OgnlException"));
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
