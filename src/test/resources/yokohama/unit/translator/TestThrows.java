package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import org.hamcrest.Matcher;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestThrows {
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
    public void Division_by_zero_1() throws Exception {
        OgnlContext env = new OgnlContext();
        Throwable actual;
        try {
            eval("1 / 0", env, "TestThrows.docy", 2, "TestThrows.docy:2.14-2.19");
            actual = null;
        } catch (OgnlException e) {
            actual = e.getReason();
        } catch (Throwable e) {
            actual = e;
        }
        Matcher expected = instanceOf(ArithmeticException.class);
        assertThat(actual, is(expected));
    }
}
