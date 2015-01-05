package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestIsNot {
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
    public void Simple_Arithmetic_1() throws Exception {
        OgnlContext env = new OgnlContext();
        {
            Object actual = eval("1 + 1", env, "TestIsNot.docy", 2, "TestIsNot.docy:2.14-2.19");
            Object unexpected = eval("3", env, "TestIsNot.docy", 2, "TestIsNot.docy:2.29-2.30");
            assertThat(actual, is(not(unexpected)));
        }
    }
}
