package yokohama.unit.ast_junit;

import java.util.Arrays;
import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.util.SBuilder;

public class ClassDeclTest {
    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        ClassDecl instance = new ClassDecl("TestClass", Arrays.asList());
        instance.toString(actual, new OgnlExpressionStrategy(), new MockitoMockStrategy());

        StrBuilder expected = new StrBuilder();
        expected.appendln("public class TestClass {");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }
}
