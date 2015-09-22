package yokohama.unit.ast_junit;

import yokohama.unit.util.Sym;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.position.Span;
import yokohama.unit.util.SBuilder;

public class MethodTest {
    public static final Annotation TEST =
            new Annotation(new ClassType(org.junit.Test.class));

    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        Method instance = new Method(
                Arrays.asList(TEST),
                "test",
                Arrays.asList(),
                Optional.empty(),
                Arrays.asList(new ClassType(java.lang.Exception.class)),
                Arrays.asList());
        instance.toString(actual);

        StrBuilder expected = new StrBuilder();
        expected.appendln("@org.junit.Test");
        expected.appendln("public void test() throws java.lang.Exception {");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

    @Test
    public void testToString_SBuilder1() {
        SBuilder actual = new SBuilder(4);
        Method instance = new Method(
                Arrays.asList(TEST),
                "test",
                Arrays.asList(),
                Optional.empty(),
                Arrays.asList(new ClassType(java.lang.Exception.class)),
                Arrays.asList(
                        new VarInitStatement(Type.STRING, Sym.of("message"), new StrLitExpr(""), Span.dummySpan()),
                        new VarInitStatement(Type.OBJECT, Sym.of("actual"), new IntLitExpr(1), Span.dummySpan()),
                        new VarInitStatement(Type.OBJECT, Sym.of("expected"), new IntLitExpr(1), Span.dummySpan()),
                        new IsStatement(Sym.of("message"), Sym.of("actual"), Sym.of("expected"), Span.dummySpan())));
        instance.toString(actual);

        StrBuilder expected = new StrBuilder();
        expected.appendln("@org.junit.Test");
        expected.appendln("public void test() throws java.lang.Exception {");
        expected.appendln("    java.lang.Object actual;");
        expected.appendln("    java.lang.Object expected;");
        expected.appendln("    java.lang.String message;");
        expected.appendln("    message = \"\";");
        expected.appendln("    actual = 1;");
        expected.appendln("    expected = 1;");
        expected.appendln("    org.junit.Assert.assertThat(message, actual, org.hamcrest.CoreMatchers.is(expected));");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

}
