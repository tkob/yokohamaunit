package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestFourPhaseWithTeardown {
    private Object eval(String expression, OgnlContext env, int startLine, int startCol, int endLine, int endCol) throws OgnlException {
        try {
            return Ognl.getValue(expression, env);
        } catch (OgnlException e) {
            Throwable reason = e.getReason();
            String pos = "dummy" + ":" + startLine + "." + startCol + "-" + endLine + "." + endCol;
            OgnlException e2 = reason == null ? new OgnlException(pos + " " + e.getMessage(), e) : new OgnlException(pos + " " + reason.getMessage(), reason);
            StackTraceElement[] st = { new StackTraceElement("", "", "dummy", startLine) };
            e2.setStackTrace(st);
            throw e2;
        }
    }
    @Test
    public void The_size_of_a_new_temporary_file_is_zero() throws Exception {
        OgnlContext env = new OgnlContext();
        try {
            env.put("temp", Ognl.getValue("@java.io.File@createTempFile(\"prefix\", null)", env));
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
