package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestMultiplePropositions {
    @Test
    public void Multiple_propositions_1() throws Exception {
        OgnlContext env = new OgnlContext();
        {
            Object actual = Ognl.getValue("1 + 1", env);
            Object expected = Ognl.getValue("2", env);
            assertThat(actual, is(expected));
        }
        {
            Object actual = Ognl.getValue("1 + 1", env);
            Object unexpected = Ognl.getValue("3", env);
            assertThat(actual, is(not(unexpected)));
        }
        try {
            Ognl.getValue("1 / 0", env);
            fail("`1 / 0` was expected to throw ArithmeticException.");
        } catch (OgnlException e) {
            assertThat(e.getReason(), is(ArithmeticException.class));
        } catch (ArithmeticException e) {
        }
    }
}
