package yokohama.unit.ast_junit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.text.StrBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.util.SBuilder;

public class TestMethodTest {
    
    @Test
    public void testImportedNames() {
        TestMethod instance = new TestMethod("test", 
                Arrays.asList(),
                Arrays.asList(),
                Arrays.asList(),
                Arrays.asList()
        );
        Set<ImportedName> expected = new HashSet<ImportedName>();
        expected.add(new ImportClass("org.junit.Test"));
        expected.add(new ImportClass("ognl.OgnlContext"));
        Set<ImportedName> actual = instance.importedNames(new OgnlExpressionStrategy(), new MockitoMockStrategy());
        assertThat(actual, is(expected));
    }

    @Test
    public void testImportedNames1() {
        TestMethod instance = new TestMethod(
                "test name",
                Arrays.asList(),
                Arrays.asList(),
                Arrays.asList(new IsStatement(new QuotedExpr("", Span.dummySpan()), new QuotedExpr("", Span.dummySpan()))),
                Arrays.asList()
        );
        Set<ImportedName> expected = new TreeSet<ImportedName>();
        expected.add(new ImportClass("org.junit.Test"));
        expected.add(new ImportClass("ognl.Ognl"));
        expected.add(new ImportClass("ognl.OgnlContext"));
        expected.add(new ImportClass("org.junit.Assert.assertThat"));
        expected.add(new ImportStatic("org.hamcrest.CoreMatchers.is"));
        Set<ImportedName> actual = instance.importedNames(new OgnlExpressionStrategy(), new MockitoMockStrategy());
        assertThat(actual, is(expected));
    }

    @Test
    public void testImportedNames2() {
        TestMethod instance = new TestMethod(
                "test name",
                Arrays.asList(),
                Arrays.asList(),
                Arrays.asList(
                        new IsStatement(new QuotedExpr("", Span.dummySpan()), new QuotedExpr("", Span.dummySpan())),
                        new IsNotStatement(new QuotedExpr("", Span.dummySpan()), new QuotedExpr("", Span.dummySpan()))),
                Arrays.asList()
        );
        Set<ImportedName> expected = new TreeSet<ImportedName>();
        expected.add(new ImportClass("org.junit.Test"));
        expected.add(new ImportClass("ognl.Ognl"));
        expected.add(new ImportClass("ognl.OgnlContext"));
        expected.add(new ImportClass("org.junit.Assert.assertThat"));
        expected.add(new ImportStatic("org.hamcrest.CoreMatchers.is"));
        expected.add(new ImportStatic("org.hamcrest.CoreMatchers.not"));
        Set<ImportedName> actual = instance.importedNames(new OgnlExpressionStrategy(), new MockitoMockStrategy());
        assertThat(actual, is(expected));
    }

    @Test
    public void testImportedNames3() {
        TestMethod instance = new TestMethod(
                "test name",
                Arrays.asList(),
                Arrays.asList(),
                Arrays.asList(
                        new IsStatement(new QuotedExpr("", Span.dummySpan()), new QuotedExpr("", Span.dummySpan())),
                        new ThrowsStatement(new QuotedExpr("", Span.dummySpan()), new QuotedExpr("", Span.dummySpan()))),
                Arrays.asList()
        );
        Set<ImportedName> expected = new TreeSet<ImportedName>();
        expected.add(new ImportClass("org.junit.Test"));
        expected.add(new ImportClass("ognl.Ognl"));
        expected.add(new ImportClass("ognl.OgnlContext"));
        expected.add(new ImportClass("ognl.OgnlException"));
        expected.add(new ImportClass("org.junit.Assert.assertThat"));
        expected.add(new ImportClass("org.junit.Assert.fail"));
        expected.add(new ImportStatic("org.hamcrest.CoreMatchers.instanceOf"));
        expected.add(new ImportStatic("org.hamcrest.CoreMatchers.is"));
        Set<ImportedName> actual = instance.importedNames(new OgnlExpressionStrategy(), new MockitoMockStrategy());
        assertThat(actual, is(expected));
    }

    @Test
    public void testToString_SBuilder() {
        SBuilder actual = new SBuilder(4);
        TestMethod instance = new TestMethod("test", Arrays.asList(), Arrays.asList(), Arrays.asList(), Arrays.asList());
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
                Arrays.asList(),
                Arrays.asList(),
                Arrays.asList(new IsStatement(new QuotedExpr("x", Span.dummySpan()), new QuotedExpr("y", Span.dummySpan()))),
                Arrays.asList());
        instance.toString(actual, new OgnlExpressionStrategy(), new MockitoMockStrategy());

        StrBuilder expected = new StrBuilder();
        expected.appendln("@Test");
        expected.appendln("public void test() throws Exception {");
        expected.appendln("    OgnlContext env = new OgnlContext();");
        expected.appendln("    {");
        expected.appendln("        Object actual = Ognl.getValue(\"x\", env);");
        expected.appendln("        Object expected = Ognl.getValue(\"y\", env);");
        expected.appendln("        assertThat(actual, is(expected));");
        expected.appendln("    }");
        expected.appendln("}");

        assertThat(actual.toString(), is(expected.toString()));
    }

}
