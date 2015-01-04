package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
        try {
            Ognl.getValue("1 / 0", env);
            fail("`1 / 0` was expected to throw ArithmeticException.");
        } catch (OgnlException e) {
            assertThat(e.getReason(), is(instanceOf(ArithmeticException.class)));
        } catch (ArithmeticException e) {
        }
    }
}
