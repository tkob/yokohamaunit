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
            env.put("temp", eval("@java.io.File@createTempFile(\"prefix\", null)", env, "TestFourPhaseWithTeardown.docy", 3, "TestFourPhaseWithTeardown.docy:3.14-3.58"));
            {
                Object actual = Ognl.getValue("temp.length()", env);
                Object expected = Ognl.getValue("0L", env);
                assertThat(actual, is(expected));
            }
        } finally {
            Ognl.getValue("temp.delete()", env);
        }
    }
}
