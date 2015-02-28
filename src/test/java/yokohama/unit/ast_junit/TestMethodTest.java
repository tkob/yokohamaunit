package yokohama.unit.ast_junit;

import java.util.Arrays;
import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.util.SBuilder;

public class TestMethodTest {
    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        TestMethod instance = new TestMethod("test", Arrays.asList(), Arrays.asList());
        instance.toString(actual, new OgnlExpressionStrategy(), new MockitoMockStrategy());

        StrBuilder expected = new StrBuilder();
        expected.appendln("@Test");
        expected.appendln("public void test() throws Exception {");
        expected.appendln("    OgnlContext env = new OgnlContext();");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

    @Test
    public void testToString_SBuilder1() {
        SBuilder actual = new SBuilder(4);
        TestMethod instance = new TestMethod(
                "test",
                Arrays.asList(
                        new VarDeclStatement("actual", new QuotedExpr("x", Span.dummySpan())),
                        new VarDeclStatement("expected", new QuotedExpr("y", Span.dummySpan())),
                        new IsStatement(new Var("actual"), new Var("expected"))),
                Arrays.asList());
        instance.toString(actual, new OgnlExpressionStrategy(), new MockitoMockStrategy());

        StrBuilder expected = new StrBuilder();
        expected.appendln("@Test");
        expected.appendln("public void test() throws Exception {");
        expected.appendln("    OgnlContext env = new OgnlContext();");
        expected.appendln("    Object actual = eval(\"x\", env, \"?\", -1, \"?:?\");");
        expected.appendln("    Object expected = eval(\"y\", env, \"?\", -1, \"?:?\");");
        expected.appendln("    assertThat(actual, is(expected));");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

}
