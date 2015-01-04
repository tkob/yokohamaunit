package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestTable {
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
    public void String_startsWith_1_1() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", Ognl.getValue("\"hello world\"", env));
        env.put("prefix", Ognl.getValue("\"\"", env));
        env.put("expected", Ognl.getValue("true", env));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_2() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", Ognl.getValue("\"hello world\"", env));
        env.put("prefix", Ognl.getValue("\"hello\"", env));
        env.put("expected", Ognl.getValue("true", env));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_3() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", Ognl.getValue("\"hello world\"", env));
        env.put("prefix", Ognl.getValue("\"world\"", env));
        env.put("expected", Ognl.getValue("false", env));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
}
