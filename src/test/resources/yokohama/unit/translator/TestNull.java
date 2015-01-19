package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import org.hamcrest.Matcher;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestNull {
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
    public void Null_test_1() throws Exception {
        OgnlContext env = new OgnlContext();
        Object actual = eval("null", env, "TestNull.docy", 2, "TestNull.docy:2.14-2.18");
        Matcher expected = nullValue();
        assertThat(actual, is(expected));
    }
    @Test
    public void Null_test_2() throws Exception {
        OgnlContext env = new OgnlContext();
        Object actual = eval("\"null\"", env, "TestNull.docy", 3, "TestNull.docy:3.14-3.20");
        Matcher unexpected = nullValue();
        assertThat(actual, is(not(unexpected)));
    }
}
