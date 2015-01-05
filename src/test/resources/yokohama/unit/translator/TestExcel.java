package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestExcel {
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
        env.put("sut", eval("\"hello world\"", env, "TestExcel.xlsx", 4, "TestExcel.xlsx:4.3"));
        env.put("prefix", eval("\"\"", env, "TestExcel.xlsx", 4, "TestExcel.xlsx:4.4"));
        env.put("expected", eval("true", env, "TestExcel.xlsx", 4, "TestExcel.xlsx:4.5"));
        {
            Object actual = eval("sut.startsWith(prefix)", env, "TestExcel.docy", 2, "TestExcel.docy:2.9-2.31");
            Object expected = eval("expected", env, "TestExcel.docy", 2, "TestExcel.docy:2.37-2.45");
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_2() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", eval("\"hello world\"", env, "TestExcel.xlsx", 5, "TestExcel.xlsx:5.3"));
        env.put("prefix", eval("\"hello\"", env, "TestExcel.xlsx", 5, "TestExcel.xlsx:5.4"));
        env.put("expected", eval("true", env, "TestExcel.xlsx", 5, "TestExcel.xlsx:5.5"));
        {
            Object actual = eval("sut.startsWith(prefix)", env, "TestExcel.docy", 2, "TestExcel.docy:2.9-2.31");
            Object expected = eval("expected", env, "TestExcel.docy", 2, "TestExcel.docy:2.37-2.45");
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_3() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", eval("\"hello world\"", env, "TestExcel.xlsx", 6, "TestExcel.xlsx:6.3"));
        env.put("prefix", eval("\"world\"", env, "TestExcel.xlsx", 6, "TestExcel.xlsx:6.4"));
        env.put("expected", eval("false", env, "TestExcel.xlsx", 6, "TestExcel.xlsx:6.5"));
        {
            Object actual = eval("sut.startsWith(prefix)", env, "TestExcel.docy", 2, "TestExcel.docy:2.9-2.31");
            Object expected = eval("expected", env, "TestExcel.docy", 2, "TestExcel.docy:2.37-2.45");
            assertThat(actual, is(expected));
        }
    }
}
