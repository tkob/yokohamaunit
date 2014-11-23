package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestBindings {
    @Test
    public void String_startsWith_returns_true_if_the_prefix_is_empty_1() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", Ognl.getValue("\"hello world\"", env));
        env.put("prefix", Ognl.getValue("\"\"", env));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("true", env);
            assertThat(actual, is(expected));
        }
    }
}
