package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import org.hamcrest.Matcher;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestMultiplePropositions {
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
    public void Multiple_propositions_1() throws Exception {
        OgnlContext env = new OgnlContext();
        Object actual = eval("1 + 1", env, "TestMultiplePropositions.docy", 2, "TestMultiplePropositions.docy:2.14-2.19");
        Object obj = eval("2", env, "TestMultiplePropositions.docy", 2, "TestMultiplePropositions.docy:2.25-2.26");
        Matcher expected = is(obj);
        assertThat(actual, is(expected));
        Object actual2 = eval("1 + 1", env, "TestMultiplePropositions.docy", 3, "TestMultiplePropositions.docy:3.14-3.19");
        Object obj2 = eval("3", env, "TestMultiplePropositions.docy", 3, "TestMultiplePropositions.docy:3.29-3.30");
        Matcher unexpected = is(obj2);
        assertThat(actual2, is(not(unexpected)));
        Throwable actual3;
        try {
            eval("1 / 0", env, "TestMultiplePropositions.docy", 4, "TestMultiplePropositions.docy:4.14-4.19");
            actual3 = null;
        } catch (OgnlException e) {
            actual3 = e.getReason();
        } catch (Throwable e) {
            actual3 = e;
        }
        Matcher expected2 = instanceOf(ArithmeticException.class);
        assertThat(actual3, is(expected2));
    }
}
