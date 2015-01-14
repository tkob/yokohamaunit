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
        Object sut = eval("\"hello world\"", env, "TestTable.docy", 7, "TestTable.docy:7.2-7.17");
        env.put("sut", sut);
        Object prefix = eval("\"\"", env, "TestTable.docy", 7, "TestTable.docy:7.18-7.31");
        env.put("prefix", prefix);
        Object expected = eval("true", env, "TestTable.docy", 7, "TestTable.docy:7.32-7.42");
        env.put("expected", expected);
        Object actual = eval("sut.startsWith(prefix)", env, "TestTable.docy", 3, "TestTable.docy:3.9-3.31");
        Object expected4 = eval("expected", env, "TestTable.docy", 3, "TestTable.docy:3.37-3.45");
        assertThat(actual, is(expected4));
    }
    @Test
    public void String_startsWith_1_2() throws Exception {
        OgnlContext env = new OgnlContext();
        Object sut2 = eval("\"hello world\"", env, "TestTable.docy", 8, "TestTable.docy:8.2-8.17");
        env.put("sut", sut2);
        Object prefix2 = eval("\"hello\"", env, "TestTable.docy", 8, "TestTable.docy:8.18-8.31");
        env.put("prefix", prefix2);
        Object expected2 = eval("true", env, "TestTable.docy", 8, "TestTable.docy:8.32-8.42");
        env.put("expected", expected2);
        Object actual2 = eval("sut.startsWith(prefix)", env, "TestTable.docy", 3, "TestTable.docy:3.9-3.31");
        Object expected5 = eval("expected", env, "TestTable.docy", 3, "TestTable.docy:3.37-3.45");
        assertThat(actual2, is(expected5));
    }
    @Test
    public void String_startsWith_1_3() throws Exception {
        OgnlContext env = new OgnlContext();
        Object sut3 = eval("\"hello world\"", env, "TestTable.docy", 9, "TestTable.docy:9.2-9.17");
        env.put("sut", sut3);
        Object prefix3 = eval("\"world\"", env, "TestTable.docy", 9, "TestTable.docy:9.18-9.31");
        env.put("prefix", prefix3);
        Object expected3 = eval("false", env, "TestTable.docy", 9, "TestTable.docy:9.32-9.42");
        env.put("expected", expected3);
        Object actual3 = eval("sut.startsWith(prefix)", env, "TestTable.docy", 3, "TestTable.docy:3.9-3.31");
        Object expected6 = eval("expected", env, "TestTable.docy", 3, "TestTable.docy:3.37-3.45");
        assertThat(actual3, is(expected6));
    }
}
