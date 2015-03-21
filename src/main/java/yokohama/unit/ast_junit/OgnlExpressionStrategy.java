package yokohama.unit.ast_junit;

import yokohama.unit.util.SBuilder;

public class OgnlExpressionStrategy implements ExpressionStrategy {
    @Override
    public void auxMethods(SBuilder sb) {
        sb.appendln("private static Object eval(String expression, ognl.OgnlContext env, String fileName, int startLine, String span) throws ognl.OgnlException {");
        sb.shift();
            sb.appendln("try {");
            sb.shift();
                sb.appendln("return ognl.Ognl.getValue(expression, env);");
            sb.unshift();
            sb.appendln("} catch (ognl.OgnlException e) {");
            sb.shift();
                sb.appendln("Throwable reason = e.getReason();");
                sb.appendln("ognl.OgnlException e2 = reason == null ? new ognl.OgnlException(span + \" \" + e.getMessage(), e)",
                                                             " : new ognl.OgnlException(span + \" \" + reason.getMessage(), reason);");
                sb.appendln("StackTraceElement[] st = { new StackTraceElement(\"\", \"\", fileName, startLine) };");
                sb.appendln("e2.setStackTrace(st);");
                sb.appendln("throw e2;");
            sb.unshift();
            sb.appendln("}");
        sb.unshift();
        sb.appendln("}");
    }
}
