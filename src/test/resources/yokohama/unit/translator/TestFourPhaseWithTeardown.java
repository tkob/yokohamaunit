package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestFourPhaseWithTeardown {
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
    public void The_size_of_a_new_temporary_file_is_zero() throws Exception {
        OgnlContext env = new OgnlContext();
        try {
            Object temp = eval("@java.io.File@createTempFile(\"prefix\", null)", env, "TestFourPhaseWithTeardown.docy", 3, "TestFourPhaseWithTeardown.docy:3.14-3.58");
            env.put("temp", temp);
            Object actual = eval("temp.length()", env, "TestFourPhaseWithTeardown.docy", 6, "TestFourPhaseWithTeardown.docy:6.9-6.22");
            Object expected = eval("0L", env, "TestFourPhaseWithTeardown.docy", 6, "TestFourPhaseWithTeardown.docy:6.28-6.30");
            assertThat(actual, is(expected));
        } finally {
            eval("temp.delete()", env, "TestFourPhaseWithTeardown.docy", 9, "TestFourPhaseWithTeardown.docy:9.5-9.18");
        }
    }
}
