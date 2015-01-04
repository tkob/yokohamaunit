package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestFourPhase {
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
    public void AtomicInteger_incrementAndGet_increments_the_content() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("i", Ognl.getValue("new java.util.concurrent.atomic.AtomicInteger(0)", env));
        Ognl.getValue("i.incrementAndGet()", env);
        {
            Object actual = Ognl.getValue("i.get()", env);
            Object expected = Ognl.getValue("1", env);
            assertThat(actual, is(expected));
        }
    }
}
