package yokohama.unit.translator;

import ognl.Ognl;
import ognl.OgnlContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TestFourPhaseWithTeardown {
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
