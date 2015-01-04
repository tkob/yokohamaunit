package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestBindings {
    private Object eval(String expression, OgnlContext env, String fileName, int startLine, String span) throws OgnlException {
        try {
            return Ognl.getValue(expression, env);
        } catch (OgnlException e) {
            Throwable reason = e.getReason();
            OgnlException e2 = reason == null ? new OgnlException(span + " " + e.getMessage(), e) : new OgnlException(span + " " + reason.getMessage(), reason);
            StackTraceElement[] st = { new StackTraceElement("", "", fileName, startLine) };
            e2.setStackTrace(st);
            throw e2;
        }
    }
    @Test
    public void String_startsWith_returns_true_if_the_prefix_is_empty_1() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", eval("\"hello world\"", env, "TestBindings.docy", 4, "TestBindings.docy:4.24-4.37"));
        env.put("prefix", eval("\"\"", env, "TestBindings.docy", 5, "TestBindings.docy:5.24-5.26"));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("true", env);
            assertThat(actual, is(expected));
        }
    }
}
