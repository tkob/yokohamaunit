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
        Object sut = eval("\"hello world\"", env, "TestExcel.xlsx", 4, "TestExcel.xlsx:4.3");
        env.put("sut", sut);
        Object prefix = eval("\"\"", env, "TestExcel.xlsx", 4, "TestExcel.xlsx:4.4");
        env.put("prefix", prefix);
        Object expected = eval("true", env, "TestExcel.xlsx", 4, "TestExcel.xlsx:4.5");
        env.put("expected", expected);
        {
            Object actual = eval("sut.startsWith(prefix)", env, "TestExcel.docy", 2, "TestExcel.docy:2.9-2.31");
            Object expected = eval("expected", env, "TestExcel.docy", 2, "TestExcel.docy:2.37-2.45");
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_2() throws Exception {
        OgnlContext env = new OgnlContext();
        Object sut2 = eval("\"hello world\"", env, "TestExcel.xlsx", 5, "TestExcel.xlsx:5.3");
        env.put("sut", sut2);
        Object prefix2 = eval("\"hello\"", env, "TestExcel.xlsx", 5, "TestExcel.xlsx:5.4");
        env.put("prefix", prefix2);
        Object expected2 = eval("true", env, "TestExcel.xlsx", 5, "TestExcel.xlsx:5.5");
        env.put("expected", expected2);
        {
            Object actual = eval("sut.startsWith(prefix)", env, "TestExcel.docy", 2, "TestExcel.docy:2.9-2.31");
            Object expected = eval("expected", env, "TestExcel.docy", 2, "TestExcel.docy:2.37-2.45");
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_3() throws Exception {
        OgnlContext env = new OgnlContext();
        Object sut3 = eval("\"hello world\"", env, "TestExcel.xlsx", 6, "TestExcel.xlsx:6.3");
        env.put("sut", sut3);
        Object prefix3 = eval("\"world\"", env, "TestExcel.xlsx", 6, "TestExcel.xlsx:6.4");
        env.put("prefix", prefix3);
        Object expected3 = eval("false", env, "TestExcel.xlsx", 6, "TestExcel.xlsx:6.5");
        env.put("expected", expected3);
        {
            Object actual = eval("sut.startsWith(prefix)", env, "TestExcel.docy", 2, "TestExcel.docy:2.9-2.31");
            Object expected = eval("expected", env, "TestExcel.docy", 2, "TestExcel.docy:2.37-2.45");
            assertThat(actual, is(expected));
        }
    }
}
