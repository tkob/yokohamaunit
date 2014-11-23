package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestExcel {
    @Test
    public void String_startsWith_1_1() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", Ognl.getValue("\"hello world\"", env));
        env.put("prefix", Ognl.getValue("\"\"", env));
        env.put("expected", Ognl.getValue("true", env));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_2() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", Ognl.getValue("\"hello world\"", env));
        env.put("prefix", Ognl.getValue("\"hello\"", env));
        env.put("expected", Ognl.getValue("true", env));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
    @Test
    public void String_startsWith_1_3() throws Exception {
        OgnlContext env = new OgnlContext();
        env.put("sut", Ognl.getValue("\"hello world\"", env));
        env.put("prefix", Ognl.getValue("\"world\"", env));
        env.put("expected", Ognl.getValue("false", env));
        {
            Object actual = Ognl.getValue("sut.startsWith(prefix)", env);
            Object expected = Ognl.getValue("expected", env);
            assertThat(actual, is(expected));
        }
    }
}
