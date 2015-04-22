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
import yokohama.unit.position.Span;
import yokohama.unit.ast.Table;
import yokohama.unit.ast.TableExtractVisitor;
import yokohama.unit.ast.ThrowsPredicate;
import yokohama.unit.ast_junit.Annotation;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.EqualOpExpr;
import yokohama.unit.ast_junit.EqualToMatcherExpr;
import yokohama.unit.ast_junit.IfStatement;
import yokohama.unit.ast_junit.InstanceOfMatcherExpr;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.IsStatement;
import yokohama.unit.ast_junit.Method;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.NullExpr;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.TryStatement;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;

public class AstToJUnitAstTest {

    @Test
    public void testTranslate() {
        String name = "TestGroup";
        Group group = new Group(Arrays.asList(), Arrays.asList(), yokohama.unit.position.Span.dummySpan());
        String packageName = "com.example";
        GenSym genSym = new GenSym();
        AstToJUnitAst instance = new AstToJUnitAst(name, packageName, new OgnlExpressionStrategy(name, packageName, genSym), new MockitoMockStrategy(genSym), genSym, new TableExtractVisitor());
        CompilationUnit actual = instance.translate(group);
        CompilationUnit expected = new CompilationUnit(packageName, Arrays.asList(new ClassDecl(true, name, Optional.empty(), Arrays.asList(), Arrays.asList())));
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslateTest() {
        yokohama.unit.ast.Test test = new yokohama.unit.ast.Test("test", Arrays.asList(), 0, yokohama.unit.position.Span.dummySpan());
        List<Table> tables = Arrays.asList();
        GenSym genSym = new GenSym();
        AstToJUnitAst instance = new AstToJUnitAst("", "", new OgnlExpressionStrategy("Name", "com.example", genSym), new MockitoMockStrategy(genSym), genSym, new TableExtractVisitor());
        List<Method> actual = instance.translateTest(test, tables, new ClassResolver());
        List<Method> expected = Arrays.asList();
        assertThat(actual, is(expected));
    }

    /**
     * TODO: More tests needed for translateAssertion
     */
    @Test
    public void testTranslateAssertion() {
        Assertion assertion = new Assertion(Arrays.asList(), Fixture.none(), yokohama.unit.position.Span.dummySpan());
        String testName = "test";
        List<Table> tables = Arrays.asList();
        GenSym genSym = new GenSym();
        AstToJUnitAst instance = new AstToJUnitAst("", "", new OgnlExpressionStrategy("Name", "com.example", genSym), new MockitoMockStrategy(genSym), genSym, new TableExtractVisitor());
        List<Method> actual = instance.translateAssertion(assertion, 0, testName, tables, new ClassResolver());
        List<Method> expected = Arrays.asList(new Method(
                Arrays.asList(Annotation.TEST),
                "test_0",
                Arrays.asList(),
                Optional.empty(),
                Arrays.asList(new ClassType("java.lang.Exception", Span.dummySpan())),
                Arrays.asList(
                        new VarInitStatement(
                                OgnlExpressionStrategy.OGNL_CONTEXT,
                                "env",
                                new NewExpr("ognl.OgnlContext"),
                                Span.dummySpan()))));
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslateProposition() {
        Proposition proposition = new Proposition(
                new yokohama.unit.ast.QuotedExpr("a", yokohama.unit.position.Span.dummySpan()),
                new IsPredicate(
                        new EqualToMatcher(
                                new yokohama.unit.ast.QuotedExpr("b", yokohama.unit.position.Span.dummySpan()),
                                yokohama.unit.position.Span.dummySpan()),
                        yokohama.unit.position.Span.dummySpan()),
                yokohama.unit.position.Span.dummySpan());
        GenSym genSym = new GenSym();
        AstToJUnitAst instance = new AstToJUnitAst("C", "p", new OgnlExpressionStrategy("Name", "com.example", genSym), new MockitoMockStrategy(genSym), genSym, new TableExtractVisitor());
        List<Statement> actual =
                instance.translateProposition(proposition, new ClassResolver(), "env")
                        .collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(
                // `a`
                new VarInitStatement(Type.STRING, "expression", new StrLitExpr("a"), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, "actual", new InvokeStaticExpr(
                        new ClassType("ognl.Ognl", Span.dummySpan()),
                        Arrays.asList(),
                        "getValue",
                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                        Arrays.asList(
                                new Var("expression"),
                                new Var("env"),
                                new Var("env")),
                        Type.OBJECT),
                        Span.dummySpan()),
                // `b`
                new VarInitStatement(Type.STRING, "expression2", new StrLitExpr("b"), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, "obj", new InvokeStaticExpr(
                        new ClassType("ognl.Ognl", Span.dummySpan()),
                        Arrays.asList(),
                        "getValue",
                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                        Arrays.asList(
                                new Var("expression2"),
                                new Var("env"),
                                new Var("env")),
                        Type.OBJECT),
                        Span.dummySpan()),
                // is `b`
                new VarInitStatement(
                        Type.MATCHER, "expected", new EqualToMatcherExpr(new Var("obj")), Span.dummySpan()),
                // `a` is `b`
                new IsStatement(new Var("actual"), new Var("expected"), Span.dummySpan()));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void testTranslateProposition1() {
        Proposition proposition = new Proposition(
                new yokohama.unit.ast.QuotedExpr("a", yokohama.unit.position.Span.dummySpan()),
                new IsNotPredicate(
                        new EqualToMatcher(
                                new yokohama.unit.ast.QuotedExpr("b", yokohama.unit.position.Span.dummySpan()),
                                yokohama.unit.position.Span.dummySpan()),
                        yokohama.unit.position.Span.dummySpan()),
                yokohama.unit.position.Span.dummySpan());

        GenSym genSym = new GenSym();
        AstToJUnitAst instance = new AstToJUnitAst("C", "p", new OgnlExpressionStrategy("Name", "com.example", genSym), new MockitoMockStrategy(genSym), genSym, new TableExtractVisitor());
        List<Statement> actual =
                instance.translateProposition(proposition, new ClassResolver(), "env")
                        .collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(
                // `a`
                new VarInitStatement(Type.STRING, "expression", new StrLitExpr("a"), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, "actual", new InvokeStaticExpr(
                        new ClassType("ognl.Ognl", Span.dummySpan()),
                        Arrays.asList(),
                        "getValue",
                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                        Arrays.asList(
                                new Var("expression"),
                                new Var("env"),
                                new Var("env")),
                        Type.OBJECT),
                        Span.dummySpan()),
                // `b`
                new VarInitStatement(Type.STRING, "expression2", new StrLitExpr("b"), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, "obj", new InvokeStaticExpr(
                        new ClassType("ognl.Ognl", Span.dummySpan()),
                        Arrays.asList(),
                        "getValue",
                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                        Arrays.asList(
                                new Var("expression2"),
                                new Var("env"),
                                new Var("env")),
                        Type.OBJECT),
                        Span.dummySpan()),
                // is not `b`
                new VarInitStatement(
                        Type.MATCHER, "unexpected", new EqualToMatcherExpr(new Var("obj")), Span.dummySpan()),
                new VarInitStatement(
                        Type.MATCHER,
                        "expected",
                        new InvokeStaticExpr(
                                new ClassType("org.hamcrest.CoreMatchers", Span.dummySpan()),
                                Arrays.asList(),
                                "not",
                                Arrays.asList(Type.MATCHER),
                                Arrays.asList(new Var("unexpected")),
                                Type.MATCHER),
                        Span.dummySpan()),
                // `a` is not `b`
                new IsStatement(new Var("actual"), new Var("expected"), Span.dummySpan()));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void testTranslateProposition2() {
        Proposition proposition = new Proposition(
                new yokohama.unit.ast.QuotedExpr("a", yokohama.unit.position.Span.dummySpan()),
                new ThrowsPredicate(
                        new InstanceOfMatcher(
                                new yokohama.unit.ast.ClassType("String", yokohama.unit.position.Span.dummySpan()),
                                yokohama.unit.position.Span.dummySpan()),
                        yokohama.unit.position.Span.dummySpan()),
                yokohama.unit.position.Span.dummySpan());
        GenSym genSym = new GenSym();
        AstToJUnitAst instance = new AstToJUnitAst("C", "p", new OgnlExpressionStrategy("Name", "com.example", genSym), new MockitoMockStrategy(genSym), genSym, new TableExtractVisitor());
        List<Statement> actual =
                instance.translateProposition(proposition, new ClassResolver(), "env")
                        .collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(
                new TryStatement(
                        Arrays.asList(
                                new VarInitStatement(Type.STRING, "expression", new StrLitExpr("a"), Span.dummySpan()),
                                new VarInitStatement(Type.OBJECT, "tmp", new InvokeStaticExpr(
                                        new ClassType("ognl.Ognl", Span.dummySpan()),
                                        Arrays.asList(),
                                        "getValue",
                                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                                        Arrays.asList(
                                                new Var("expression"),
                                                new Var("env"),
                                                new Var("env")),
                                        Type.OBJECT),
                                        Span.dummySpan()),
                                new VarInitStatement(Type.THROWABLE, "actual", new NullExpr(), Span.dummySpan())),
                        Arrays.asList(
                                new CatchClause(
                                        new ClassType("ognl.OgnlException", Span.dummySpan()),
                                        new Var("ex2"),
                                        Arrays.asList(
                                                new VarInitStatement(
                                                        Type.THROWABLE,
                                                        "reason",
                                                        new InvokeExpr(
                                                                InvokeExpr.Instruction.VIRTUAL,
                                                                new Var("ex2"),
                                                                "getReason",
                                                                Arrays.asList(),
                                                                Arrays.asList(),
                                                                Type.THROWABLE),
                                                        Span.dummySpan()),
                                                new VarInitStatement(
                                                        Type.THROWABLE,
                                                        "nullValue",
                                                        new NullExpr(),
                                                        Span.dummySpan()),
                                                new VarInitStatement(
                                                        Type.BOOLEAN,
                                                        "cond",
                                                        new EqualOpExpr(new Var("reason"), new Var("nullValue")),
                                                        Span.dummySpan()),
                                                new IfStatement(
                                                        new Var("cond"),
                                                        Arrays.asList(
                                                                new VarInitStatement(
                                                                        Type.THROWABLE,
                                                                        "actual",
                                                                        new VarExpr("ex2"),
                                                                        Span.dummySpan())),
                                                        Arrays.asList(
                                                                new VarInitStatement(
                                                                        Type.THROWABLE,
                                                                        "actual",
                                                                        new VarExpr("reason"),
                                                                        Span.dummySpan()))))),
                                new CatchClause(
                                        new ClassType("java.lang.Throwable", Span.dummySpan()),
                                        new Var("ex"),
                                        Arrays.asList(
                                                new VarInitStatement(
                                                        Type.THROWABLE, "actual", new VarExpr("ex"), Span.dummySpan())))
                        ),
                        Arrays.asList()),
                new VarInitStatement(Type.MATCHER, "expected", new InstanceOfMatcherExpr("java.lang.String"), Span.dummySpan()),
                new IsStatement(new Var("actual"), new Var("expected"), Span.dummySpan()));
        assertThat(actual, is(expected));
    }
    
}
