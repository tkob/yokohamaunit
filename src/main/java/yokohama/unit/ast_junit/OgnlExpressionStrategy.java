package yokohama.unit.ast_junit;

import java.util.Optional;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import yokohama.unit.util.SBuilder;

public class OgnlExpressionStrategy implements ExpressionStrategy {
    @Override
    public void auxMethods(SBuilder sb) {
        sb.appendln("private Object eval(String expression, ognl.OgnlContext env, String fileName, int startLine, String span) throws ognl.OgnlException {");
        sb.shift();
            sb.appendln("try {");
            sb.shift();
                sb.appendln("return ognl.Ognl.getValue(expression, env);");
            sb.unshift();
            sb.appendln("} catch (ognl.OgnlException e) {");
            sb.shift();
                sb.appendln("Throwable reason = e.getReason();");
                sb.appendln("ognl.OgnlException e2 = reason == null ? newognl. OgnlException(span + \" \" + e.getMessage(), e)",
                                                             " : new ognl.OgnlException(span + \" \" + reason.getMessage(), reason);");
                sb.appendln("StackTraceElement[] st = { new StackTraceElement(\"\", \"\", fileName, startLine) };");
                sb.appendln("e2.setStackTrace(st);");
                sb.appendln("throw e2;");
            sb.unshift();
            sb.appendln("}");
        sb.unshift();
        sb.appendln("}");
    }

    @Override
    public String environment() {
        return "ognl.OgnlContext env = new ognl.OgnlContext();";
    }

    @Override
    public void bind(SBuilder sb, String name, Var varExpr) {
        sb.appendln("env.put(\"", escapeJava(name), "\", ", varExpr.getName(), ");");
    }

    @Override
    public String getValue(QuotedExpr quotedExpr) {
        return "eval(\"" + escapeJava(quotedExpr.getText()) + "\", env, " +
                "\"" + escapeJava(quotedExpr.getSpan().getFileName()) +  "\", " +
                quotedExpr.getSpan().getStart().getLine() + ", " +
                "\"" + escapeJava(quotedExpr.getSpan().toString()) + "\")";
    }

    @Override
    public Optional<String> wrappingException() {
        return Optional.of("ognl.OgnlException");
    }

    @Override
    public String wrappedException(String e) {
        return e + ".getReason()";
    }
}
