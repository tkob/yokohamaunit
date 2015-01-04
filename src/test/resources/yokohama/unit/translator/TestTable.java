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
        env.put("sut", eval("\"hello world\"", env, "TestTable.docy", 7, "TestTable.docy:7.2-7.17"));
        env.put("prefix", eval("\"\"", env, "TestTable.docy", 7, "TestTable.docy:7.18-7.31"));
        env.put("expected", eval("true", env, "TestTable.docy", 7, "TestTable.docy:7.32-7.42"));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_2() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", eval("\"hello world\"", env, "TestTable.docy", 8, "TestTable.docy:8.2-8.17"));
        env.put("prefix", eval("\"hello\"", env, "TestTable.docy", 8, "TestTable.docy:8.18-8.31"));
        env.put("expected", eval("true", env, "TestTable.docy", 8, "TestTable.docy:8.32-8.42"));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_3() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", eval("\"hello world\"", env, "TestTable.docy", 9, "TestTable.docy:9.2-9.17"));
        env.put("prefix", eval("\"world\"", env, "TestTable.docy", 9, "TestTable.docy:9.18-9.31"));
        env.put("expected", eval("false", env, "TestTable.docy", 9, "TestTable.docy:9.32-9.42"));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
}
