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
        TestMethod instance = new TestMethod("test", Arrays.asList(), Arrays.asList(), Arrays.asList());
        instance.toString(actual, new OgnlExpressionStrategy(), new MockitoMockStrategy());

        StrBuilder expected = new StrBuilder();
        expected.appendln("@org.junit.Test");
        expected.appendln("public void test() throws Exception {");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

    @Test
    public void testToString_SBuilder1() {
        SBuilder actual = new SBuilder(4);
        TestMethod instance = new TestMethod(
                "test",
                Arrays.asList(
                        new VarInitStatement("env", new NewExpr("ognl.OgnlContext"))),
                Arrays.asList(
                        new VarInitStatement("actual", new QuotedExpr("x", Span.dummySpan())),
                        new VarInitStatement("expected", new QuotedExpr("y", Span.dummySpan())),
                        new IsStatement(new Var("actual"), new Var("expected"))),
                Arrays.asList());
        instance.toString(actual, new OgnlExpressionStrategy(), new MockitoMockStrategy());

        StrBuilder expected = new StrBuilder();
        expected.appendln("@org.junit.Test");
        expected.appendln("public void test() throws Exception {");
        expected.appendln("    ognl.OgnlContext env = new ognl.OgnlContext();");
        expected.appendln("    Object actual = eval(\"x\", env, \"?\", -1, \"?:?\");");
        expected.appendln("    Object expected = eval(\"y\", env, \"?\", -1, \"?:?\");");
        expected.appendln("    org.junit.Assert.assertThat(actual, org.hamcrest.CoreMatchers.is(expected));");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

}
