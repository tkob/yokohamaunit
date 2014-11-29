package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestThrows {
    @Test
    public void Division_by_zero_1() throws Exception {
        OgnlContext env = new OgnlContext();
        try {
            Ognl.getValue("1 / 0", env);
            fail("`1 / 0` was expected to throw ArithmeticException.");
        } catch (OgnlException e) {
            assertThat(e.getReason(), is(ArithmeticException.class));
        } catch (ArithmeticException e) {
        }
    }
}
