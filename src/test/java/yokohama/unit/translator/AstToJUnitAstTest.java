package yokohama.unit.translator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.EqualToMatcher;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.InstanceOfMatcher;
import yokohama.unit.ast.IsNotPredicate;
import yokohama.unit.ast.IsPredicate;
import yokohama.unit.ast.Proposition;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.ThrowsPredicate;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.EqualToMatcherExpr;
import yokohama.unit.ast_junit.InstanceOfMatcherExpr;
import yokohama.unit.ast_junit.IntLitExpr;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.IsNotStatement;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.NullExpr;
import yokohama.unit.ast_junit.Span;
import yokohama.unit.ast_junit.TestMethod;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.TryStatement;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.util.GenSym;

public class AstToJUnitAstTest {

    @Test
    public void testTranslate() {
        String name = "TestGroup";
        Group group = new Group(Arrays.asList(), yokohama.unit.ast.Span.dummySpan());
        String packageName = "com.example";
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty(), name, packageName, new OgnlExpressionStrategy(), new MockitoMockStrategy());
        CompilationUnit actual = instance.translate(name, group, packageName);
        CompilationUnit expected = new CompilationUnit(packageName, new ClassDecl(name, Arrays.asList()));
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslateTest() {
        yokohama.unit.ast.Test test = new yokohama.unit.ast.Test("test", Arrays.asList(), 0, yokohama.unit.ast.Span.dummySpan());
        List<Table> tables = Arrays.asList();
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty(), "", "", new OgnlExpressionStrategy(), new MockitoMockStrategy());
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
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty(), "", "", new OgnlExpressionStrategy(), new MockitoMockStrategy());
        List<TestMethod> actual = instance.translateAssertion(assertion, 0, testName, tables);
        List<TestMethod> expected = Arrays.asList(new TestMethod(
                "test_0",
                Arrays.asList(
                        new VarInitStatement(
                                OgnlExpressionStrategy.OGNL_CONTEXT,
                                "env",
                                new NewExpr("ognl.OgnlContext")))));
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
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty(), "C", "p", new OgnlExpressionStrategy(), new MockitoMockStrategy());
        List<Statement> actual = instance.translateProposition(proposition, new GenSym(), "env").collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(
                // `a`
                new VarInitStatement(Type.STRING, "expression", new StrLitExpr("a")),
                new VarInitStatement(Type.STRING, "fileName", new StrLitExpr("?")),
                new VarInitStatement(Type.INT, "line", new IntLitExpr(-1)),
                new VarInitStatement(Type.STRING, "span", new StrLitExpr("?:?")),
                new VarInitStatement(Type.OBJECT, "actual", new InvokeStaticExpr(
                        new ClassType("p.C", Span.dummySpan()), Arrays.asList(), "eval",
                        Arrays.asList(
                                new Var("expression"),
                                new Var("env"),
                                new Var("fileName"),
                                new Var("line"),
                                new Var("span")))),
                // `b`
                new VarInitStatement(Type.STRING, "expression2", new StrLitExpr("b")),
                new VarInitStatement(Type.STRING, "fileName2", new StrLitExpr("?")),
                new VarInitStatement(Type.INT, "line2", new IntLitExpr(-1)),
                new VarInitStatement(Type.STRING, "span2", new StrLitExpr("?:?")),
                new VarInitStatement(Type.OBJECT, "obj", new InvokeStaticExpr(
                        new ClassType("p.C", Span.dummySpan()), Arrays.asList(), "eval",
                        Arrays.asList(
                                new Var("expression2"),
                                new Var("env"),
                                new Var("fileName2"),
                                new Var("line2"),
                                new Var("span2")))),
                // is `b`
                new VarInitStatement(Type.MATCHER, "expected", new EqualToMatcherExpr(new Var("obj"))),
                // `a` is `b`
                new IsStatement(new Var("actual"), new Var("expected")));
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

        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty(), "C", "p", new OgnlExpressionStrategy(), new MockitoMockStrategy());
        List<Statement> actual = instance.translateProposition(proposition, new GenSym(), "env").collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(
                // `a`
                new VarInitStatement(Type.STRING, "expression", new StrLitExpr("a")),
                new VarInitStatement(Type.STRING, "fileName", new StrLitExpr("?")),
                new VarInitStatement(Type.INT, "line", new IntLitExpr(-1)),
                new VarInitStatement(Type.STRING, "span", new StrLitExpr("?:?")),
                new VarInitStatement(Type.OBJECT, "actual", new InvokeStaticExpr(
                        new ClassType("p.C", Span.dummySpan()), Arrays.asList(), "eval",
                        Arrays.asList(
                                new Var("expression"),
                                new Var("env"),
                                new Var("fileName"),
                                new Var("line"),
                                new Var("span")))),
                // `b`
                new VarInitStatement(Type.STRING, "expression2", new StrLitExpr("b")),
                new VarInitStatement(Type.STRING, "fileName2", new StrLitExpr("?")),
                new VarInitStatement(Type.INT, "line2", new IntLitExpr(-1)),
                new VarInitStatement(Type.STRING, "span2", new StrLitExpr("?:?")),
                new VarInitStatement(Type.OBJECT, "obj", new InvokeStaticExpr(
                        new ClassType("p.C", Span.dummySpan()), Arrays.asList(), "eval",
                        Arrays.asList(
                                new Var("expression2"),
                                new Var("env"),
                                new Var("fileName2"),
                                new Var("line2"),
                                new Var("span2")))),
                // is not `b`
                new VarInitStatement(Type.MATCHER, "unexpected", new EqualToMatcherExpr(new Var("obj"))),
                // `a` is not `b`
                new IsNotStatement(new Var("actual"), new Var("unexpected")));
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
        AstToJUnitAst instance = new AstToJUnitAst(Optional.empty(), "C", "p", new OgnlExpressionStrategy(), new MockitoMockStrategy());
        List<Statement> actual = instance.translateProposition(proposition, new GenSym(), "env").collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(
                new TryStatement(
                        Arrays.asList(
                                new VarInitStatement(Type.STRING, "expression", new StrLitExpr("a")),
                                new VarInitStatement(Type.STRING, "fileName", new StrLitExpr("?")),
                                new VarInitStatement(Type.INT, "line", new IntLitExpr(-1)),
                                new VarInitStatement(Type.STRING, "span", new StrLitExpr("?:?")),
                                new VarInitStatement(Type.OBJECT, "tmp", new InvokeStaticExpr(
                                        new ClassType("p.C", Span.dummySpan()), Arrays.asList(), "eval",
                                        Arrays.asList(
                                                new Var("expression"),
                                                new Var("env"),
                                                new Var("fileName"),
                                                new Var("line"),
                                                new Var("span")))),
                                new VarInitStatement(Type.THROWABLE, "actual", new NullExpr())),
                        Arrays.asList(
                                new CatchClause(
                                        new ClassType("ognl.OgnlException", Span.dummySpan()),
                                        new Var("ex"),
                                        Arrays.asList(
                                                new VarInitStatement(Type.THROWABLE, "cause", new InvokeExpr(new Var("ex"), "getReason", Arrays.asList())),
                                                new VarInitStatement(
                                                        Type.THROWABLE,
                                                        "actual",
                                                        new VarExpr("cause")))),
                                new CatchClause(
                                        new ClassType("java.lang.Throwable", Span.dummySpan()),
                                        new Var("ex"),
                                        Arrays.asList(
                                                new VarInitStatement(Type.THROWABLE, "actual", new VarExpr("ex"))))
                        ),
                        Arrays.asList()),
                new VarInitStatement(Type.MATCHER, "expected", new InstanceOfMatcherExpr("b")),
                new IsStatement(new Var("actual"), new Var("expected")));
        assertThat(actual, is(expected));
    }
    
}
