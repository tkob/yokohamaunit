package yokohama.unit.translator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Definition;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.InstanceOfMatcher;
import yokohama.unit.ast.IsNotPredicate;
import yokohama.unit.ast.IsPredicate;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Row;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.ThrowsPredicate;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.QuotedExpr;
import yokohama.unit.ast_junit.Span;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.ThrowsStatement;
import yokohama.unit.ast_junit.VarDeclStatement;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.util.GenSym;

public class AstToJUnitAstTest {

    @Test
    public void testExtractTables() {
        List<Definition> definitions = Arrays.asList();
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty());
        List<Table> result = instance.extractTables(definitions);
        assertThat(result.size(), is(0));
    }

    @Test
    public void testExtractTables1() {
        List<Definition> definitions = Arrays.asList(
                new Table("table 1", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.ast.Span.dummySpan())), yokohama.unit.ast.Span.dummySpan()),
                new yokohama.unit.ast.Test("test name", Arrays.asList(), 0, yokohama.unit.ast.Span.dummySpan()),
                new Table("table 2", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.ast.Span.dummySpan())), yokohama.unit.ast.Span.dummySpan())
        );
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty());
        List<Table> actual = instance.extractTables(definitions);
        List<Table> expected = Arrays.asList(
                new Table("table 1", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.ast.Span.dummySpan())), yokohama.unit.ast.Span.dummySpan()),
                new Table("table 2", Arrays.asList("a"), Arrays.asList(new Row(Arrays.asList(), yokohama.unit.ast.Span.dummySpan())), yokohama.unit.ast.Span.dummySpan())
        );
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslate() {
        String name = "TestGroup";
        Group group = new Group(Arrays.asList(), yokohama.unit.ast.Span.dummySpan());
        String packageName = "com.example";
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty());
        CompilationUnit actual = instance.translate(name, group, packageName);
        CompilationUnit expected = new CompilationUnit(packageName, new ClassDecl(name, Arrays.asList()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslateTest() {
        yokohama.unit.ast.Test test = new yokohama.unit.ast.Test("test", Arrays.asList(), 0, yokohama.unit.ast.Span.dummySpan());
        List<Table> tables = Arrays.asList();
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty());
        List<TestMethod> actual = instance.translateTest(test, tables);
        List<TestMethod> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    /**
     * TODO: More tests needed for translateAssertion
     */
    @Test
    public void testTranslateAssertion() {
        Assertion assertion = new Assertion(Arrays.asList(), Fixture.none(), yokohama.unit.ast.Span.dummySpan());
        String testName = "test";
        List<Table> tables = Arrays.asList();
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty());
        List<TestMethod> actual = instance.translateAssertion(assertion, 0, testName, tables);
        List<TestMethod> expected = Arrays.asList(new TestMethod("test_0", Arrays.asList(), Arrays.asList()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslateProposition() {
        Proposition proposition = new Proposition(
                new yokohama.unit.ast.QuotedExpr("a", yokohama.unit.ast.Span.dummySpan()),
                new IsPredicate(
                        new EqualToMatcher(
                                new yokohama.unit.ast.QuotedExpr("b", yokohama.unit.ast.Span.dummySpan()),
                                yokohama.unit.ast.Span.dummySpan()),
                        yokohama.unit.ast.Span.dummySpan()),
                yokohama.unit.ast.Span.dummySpan());
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty());
        List<Statement> actual = instance.translateProposition(proposition, new GenSym()).collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(
                new VarDeclStatement("actual", new QuotedExpr("a", Span.dummySpan())),
                new VarDeclStatement("expected", new QuotedExpr("b", Span.dummySpan())),
                new IsStatement(new VarExpr("actual"), new VarExpr("expected")));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void testTranslateProposition1() {
        Proposition proposition = new Proposition(
                new yokohama.unit.ast.QuotedExpr("a", yokohama.unit.ast.Span.dummySpan()),
                new IsNotPredicate(
                        new EqualToMatcher(
                                new yokohama.unit.ast.QuotedExpr("b", yokohama.unit.ast.Span.dummySpan()),
                                yokohama.unit.ast.Span.dummySpan()),
                        yokohama.unit.ast.Span.dummySpan()),
                yokohama.unit.ast.Span.dummySpan());

        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty());
        List<Statement> actual = instance.translateProposition(proposition, new GenSym()).collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(new IsNotStatement(
                new QuotedExpr("a", Span.dummySpan()),
                new QuotedExpr("b", Span.dummySpan())));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void testTranslateProposition2() {
        Proposition proposition = new Proposition(
                new yokohama.unit.ast.QuotedExpr("a", yokohama.unit.ast.Span.dummySpan()),
                new ThrowsPredicate(
                        new InstanceOfMatcher(
                                new yokohama.unit.ast.ClassType("b", yokohama.unit.ast.Span.dummySpan()),
                                yokohama.unit.ast.Span.dummySpan()),
                        yokohama.unit.ast.Span.dummySpan()),
                yokohama.unit.ast.Span.dummySpan());
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty());
        List<Statement> actual = instance.translateProposition(proposition, new GenSym()).collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(new ThrowsStatement(
                new QuotedExpr("a", Span.dummySpan()),
                new QuotedExpr("b", Span.dummySpan())));
        assertThat(actual, is(expected));
    }
    
}
