package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestFourPhase {
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
