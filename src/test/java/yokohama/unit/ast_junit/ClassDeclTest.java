package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.util.SBuilder;

public class ClassDeclTest {
    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        ClassDecl instance = new ClassDecl(true, "TestClass", Optional.empty(), Arrays.asList(), Arrays.asList());
        instance.toString(actual);

        StrBuilder expected = new StrBuilder();
        expected.appendln("public class TestClass {");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }
}
