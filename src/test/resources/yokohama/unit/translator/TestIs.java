package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestIs {
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
    public void Simple_Arithmetic_1() throws Exception {
        OgnlContext env = new OgnlContext();
        {
            Object actual = Ognl.getValue("1 + 1", env);
            Object expected = Ognl.getValue("2", env);
            assertThat(actual, is(expected));
        }
    }
}
