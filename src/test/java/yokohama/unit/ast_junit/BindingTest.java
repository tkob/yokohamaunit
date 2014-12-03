package yokohama.unit.ast_junit;

import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;
import yokohama.unit.util.SBuilder;

public class BindingTest {
    
    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        Binding instance = new Binding("x", "y");
        instance.toString(actual, new OgnlExpressionStrategy());

        StrBuilder expected = new StrBuilder();
        expected.appendln("env.put(\"x\", Ognl.getValue(\"y\", env));");

        assertThat(actual.toString(), is(expected.toString()));
    }
    
}
