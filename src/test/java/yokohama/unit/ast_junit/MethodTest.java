package yokohama.unit.ast_junit;

import java.util.Arrays;
import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.util.SBuilder;

public class MethodTest {
    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        Method instance = new Method(Arrays.asList(Annotation.TEST), "test", Arrays.asList(), Arrays.asList());
        instance.toString(actual);

        StrBuilder expected = new StrBuilder();
        expected.appendln("@org.junit.Test");
        expected.appendln("public void test() throws Exception {");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

    @Test
    public void testToString_SBuilder1() {
        SBuilder actual = new SBuilder(4);
        Method instance = new Method(
                Arrays.asList(Annotation.TEST),
                "test",
                Arrays.asList(),
                Arrays.asList(
                        new VarInitStatement(
                                new Type(new ClassType("ognl.OgnlContext", Span.dummySpan()), 0),
                                "env",
                                new NewExpr("ognl.OgnlContext"),
                                Span.dummySpan()),
                        new VarInitStatement(Type.OBJECT, "actual", new IntLitExpr(1), Span.dummySpan()),
                        new VarInitStatement(Type.OBJECT, "expected", new IntLitExpr(1), Span.dummySpan()),
                        new IsStatement(new Var("actual"), new Var("expected"), Span.dummySpan())));
        instance.toString(actual);

        StrBuilder expected = new StrBuilder();
        expected.appendln("@org.junit.Test");
        expected.appendln("public void test() throws Exception {");
        expected.appendln("    java.lang.Object actual;");
        expected.appendln("    ognl.OgnlContext env;");
        expected.appendln("    java.lang.Object expected;");
        expected.appendln("    env = new ognl.OgnlContext();");
        expected.appendln("    actual = 1;");
        expected.appendln("    expected = 1;");
        expected.appendln("    org.junit.Assert.assertThat(actual, org.hamcrest.CoreMatchers.is(expected));");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

}
