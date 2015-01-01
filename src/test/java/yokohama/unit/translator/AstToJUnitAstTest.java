package yokohama.unit.translator;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Copula;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Table;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.TestStatement;
import yokohama.unit.ast_junit.ThrowsStatement;

public class AstToJUnitAstTest {

    @Test
    public void testExtractTables() {
        List<Definition> definitions = Arrays.asList();
        AstToJUnitAst instance = new AstToJUnitAst();
        List<Table> result = instance.extractTables(definitions);
        assertThat(result.size(), is(0));
    }

    @Test
    public void testExtractTables1() {
        List<Definition> definitions = Arrays.asList(
                new Table("table 1", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList()))),
                new yokohama.unit.ast.Test("test name", Arrays.asList(), 0),
                new Table("table 2", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList())))
        );
        AstToJUnitAst instance = new AstToJUnitAst();
        List<Table> actual = instance.extractTables(definitions);
        List<Table> expected = Arrays.asList(
                new Table("table 1", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList()))),
                new Table("table 2", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList())))
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslate() {
        String name = "TestGroup";
        Group group = new Group(Arrays.asList());
        String packageName = "com.example";
        AstToJUnitAst instance = new AstToJUnitAst();
        CompilationUnit actual = instance.translate(name, group, packageName);
        CompilationUnit expected = new CompilationUnit(packageName, new ClassDecl(name, Arrays.asList()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslateTest() {
        yokohama.unit.ast.Test test = new yokohama.unit.ast.Test("test", Arrays.asList(), 0);
        List<Table> tables = Arrays.asList();
        AstToJUnitAst instance = new AstToJUnitAst();
        List<TestMethod> actual = instance.translateTest(test, tables);
        List<TestMethod> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    /**
     * TODO: More tests needed for translateAssertion
     */
    @Test
    public void testTranslateAssertion() {
        Assertion assertion = new Assertion(Arrays.asList(), Fixture.none());
        String testName = "test";
        List<Table> tables = Arrays.asList();
        AstToJUnitAst instance = new AstToJUnitAst();
        List<TestMethod> actual = instance.translateAssertion(assertion, 0, testName, tables);
        List<TestMethod> expected = Arrays.asList(new TestMethod("test_0", Arrays.asList(), Arrays.asList(), Arrays.asList(), Arrays.asList()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslateProposition() {
        Proposition proposition = new Proposition(new QuotedExpr("a"), Copula.IS, new QuotedExpr("b"));
        AstToJUnitAst instance = new AstToJUnitAst();
        TestStatement actual = instance.translateProposition(proposition);
        TestStatement expected = new IsStatement("a", "b");
        assertThat(actual, is(expected));
    }
    
    @Test
    public void testTranslateProposition1() {
        Proposition proposition = new Proposition(new QuotedExpr("a"), Copula.IS_NOT, new QuotedExpr("b"));
        AstToJUnitAst instance = new AstToJUnitAst();
        TestStatement actual = instance.translateProposition(proposition);
        TestStatement expected = new IsNotStatement("a", "b");
        assertThat(actual, is(expected));
    }
    
    @Test
    public void testTranslateProposition2() {
        Proposition proposition = new Proposition(new QuotedExpr("a"), Copula.THROWS, new QuotedExpr("b"));
        AstToJUnitAst instance = new AstToJUnitAst();
        TestStatement actual = instance.translateProposition(proposition);
        TestStatement expected = new ThrowsStatement("a", "b");
        assertThat(actual, is(expected));
    }
    
}
