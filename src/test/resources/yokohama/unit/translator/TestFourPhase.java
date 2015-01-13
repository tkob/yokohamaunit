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
        Object i = eval("new java.util.concurrent.atomic.AtomicInteger(0)", env, "TestFourPhase.docy", 3, "TestFourPhase.docy:3.11-3.59");
        env.put("i", i);
        eval("i.incrementAndGet()", env, "TestFourPhase.docy", 6, "TestFourPhase.docy:6.5-6.24");
        {
            Object actual = eval("i.get()", env, "TestFourPhase.docy", 9, "TestFourPhase.docy:9.9-9.16");
            Object expected = eval("1", env, "TestFourPhase.docy", 9, "TestFourPhase.docy:9.22-9.23");
            assertThat(actual, is(expected));
        }
    }
}
