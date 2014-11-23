package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestIsNot {
    @Test
    public void Simple_Arithmetic_1() throws Exception {
        OgnlContext env = new OgnlContext();
        {
            Object actual = Ognl.getValue("1 + 1", env);
            Object unexpected = Ognl.getValue("3", env);
            assertThat(actual, is(not(unexpected)));
        }
    }
}
