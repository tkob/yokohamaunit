package yokohama.unit.translator;

import java.util.Arrays;
import java.util.Collections;
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
import yokohama.unit.util.Sym;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;

public class AstToJUnitAstTest {
    static final Type MATCHER = new Type(new ClassType(org.hamcrest.Matcher.class), 0);
    static final Annotation TEST = new Annotation(new ClassType(org.junit.Test.class));

    @Test
    public void testTranslate() {
        String name = "TestGroup";
        Group group = new Group(Arrays.asList(), Arrays.asList(), yokohama.unit.position.Span.dummySpan());
        String packageName = "com.example";
        GenSym genSym = new GenSym();
        AstToJUnitAst instance = new AstToJUnitAst(name, packageName, new OgnlExpressionStrategy(name, packageName, genSym, new ClassResolver()), new MockitoMockStrategy("Name", "com.example", genSym, new ClassResolver()), genSym, new ClassResolver(), new TableExtractVisitor());
        CompilationUnit actual = instance.translate(group);
        CompilationUnit expected = new CompilationUnit(packageName, Arrays.asList(new ClassDecl(true, name, Optional.empty(), Arrays.asList(), Arrays.asList())));
        assertThat(actual, is(expected));
    }

    @Test
    public void testTranslateTest() {
        yokohama.unit.ast.Test test = new yokohama.unit.ast.Test("test", Arrays.asList(), yokohama.unit.position.Span.dummySpan());
        GenSym genSym = new GenSym();
        AstToJUnitAstVisitor instance =
                new AstToJUnitAstVisitor(
                        "",
                        "",
                        new OgnlExpressionStrategy("Name", "com.example", genSym, new ClassResolver()),
                        new MockitoMockStrategy("Name", "com.example", genSym, new ClassResolver()),
                        genSym,
                        new ClassResolver(),
                        Collections.emptyList(),
                        Collections.emptyMap());
        List<Method> actual = instance.translateTest(test);
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
        GenSym genSym = new GenSym();
        AstToJUnitAstVisitor instance =
                new AstToJUnitAstVisitor(
                        "",
                        "",
                        new OgnlExpressionStrategy("Name", "com.example", genSym, new ClassResolver()),
                        new MockitoMockStrategy("Name", "com.example", genSym, new ClassResolver()),
                        genSym,
                        new ClassResolver(),
                        Collections.emptyList(),
                        Collections.emptyMap());
        List<List<Statement>> actual = instance.translateAssertion(assertion, Sym.of("env"));
        List<List<Statement>> expected = Arrays.asList(Arrays.asList());
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
        AstToJUnitAstVisitor instance =
                new AstToJUnitAstVisitor(
                        "C",
                        "p",
                        new OgnlExpressionStrategy("Name", "com.example", genSym, new ClassResolver()),
                        new MockitoMockStrategy("Name", "com.example", genSym, new ClassResolver()),
                        genSym,
                        new ClassResolver(),
                        Collections.emptyList(),
                        Collections.emptyMap());
        List<Statement> actual =
                instance.translateProposition(proposition, Sym.of("env"))
                        .collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(// `a`
                new VarInitStatement(Type.STRING, Sym.of("expression"), new StrLitExpr("a"), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, Sym.of("expr"), new InvokeStaticExpr(
                        new ClassType(ognl.Ognl.class),
                        Arrays.asList(),
                        "getValue",
                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                        Arrays.asList(Sym.of("expression"),
                                Sym.of("env"),
                                Sym.of("env")),
                        Type.OBJECT),
                        Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, Sym.of("actual"), new VarExpr(Sym.of("expr")), Span.dummySpan()),
                // `b`
                new VarInitStatement(Type.STRING, Sym.of("expression2"), new StrLitExpr("b"), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, Sym.of("expr2"), new InvokeStaticExpr(
                        new ClassType(ognl.Ognl.class),
                        Arrays.asList(),
                        "getValue",
                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                        Arrays.asList(Sym.of("expression2"),
                                Sym.of("env"),
                                Sym.of("env")),
                        Type.OBJECT),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.OBJECT, Sym.of("obj"), new VarExpr(Sym.of("expr2")), Span.dummySpan()),
                // is `b`
                new VarInitStatement(
                        MATCHER, Sym.of("expected"), new EqualToMatcherExpr(Sym.of("obj")), Span.dummySpan()),
                // `a` is `b`
                new IsStatement(Sym.of("actual"), Sym.of("expected"), Span.dummySpan()));
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
        AstToJUnitAstVisitor instance =
                new AstToJUnitAstVisitor(
                        "C",
                        "p",
                        new OgnlExpressionStrategy("Name", "com.example", genSym, new ClassResolver()),
                        new MockitoMockStrategy("Name", "com.example", genSym, new ClassResolver()),
                        genSym,
                        new ClassResolver(),
                        Collections.emptyList(),
                        Collections.emptyMap());
        List<Statement> actual =
                instance.translateProposition(proposition, Sym.of("env"))
                        .collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(// `a`
                new VarInitStatement(Type.STRING, Sym.of("expression"), new StrLitExpr("a"), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, Sym.of("expr"), new InvokeStaticExpr(
                        new ClassType(ognl.Ognl.class),
                        Arrays.asList(),
                        "getValue",
                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                        Arrays.asList(Sym.of("expression"),
                                Sym.of("env"),
                                Sym.of("env")),
                        Type.OBJECT),
                        Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, Sym.of("actual"), new VarExpr(Sym.of("expr")), Span.dummySpan()),
                // `b`
                new VarInitStatement(Type.STRING, Sym.of("expression2"), new StrLitExpr("b"), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, Sym.of("expr2"), new InvokeStaticExpr(
                        new ClassType(ognl.Ognl.class),
                        Arrays.asList(),
                        "getValue",
                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                        Arrays.asList(Sym.of("expression2"),
                                Sym.of("env"),
                                Sym.of("env")),
                        Type.OBJECT),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.OBJECT, Sym.of("obj"), new VarExpr(Sym.of("expr2")), Span.dummySpan()),
                // is not `b`
                new VarInitStatement(
                        MATCHER, Sym.of("unexpected"), new EqualToMatcherExpr(Sym.of("obj")), Span.dummySpan()),
                new VarInitStatement(
                        MATCHER,
                        Sym.of("expected"),
                        new InvokeStaticExpr(
                                new ClassType(org.hamcrest.CoreMatchers.class),
                                Arrays.asList(),
                                "not",
                                Arrays.asList(MATCHER),
                                Arrays.asList(Sym.of("unexpected")),
                                MATCHER),
                        Span.dummySpan()),
                // `a` is not `b`
                new IsStatement(Sym.of("actual"), Sym.of("expected"), Span.dummySpan()));
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
        AstToJUnitAstVisitor instance =
                new AstToJUnitAstVisitor(
                        "C",
                        "p",
                        new OgnlExpressionStrategy("Name", "com.example", genSym, new ClassResolver()),
                        new MockitoMockStrategy("Name", "com.example", genSym, new ClassResolver()),
                        genSym,
                        new ClassResolver(),
                        Collections.emptyList(),
                        Collections.emptyMap());
        List<Statement> actual =
                instance.translateProposition(proposition, Sym.of("env"))
                        .collect(Collectors.toList());
        List<Statement> expected = Arrays.asList(new TryStatement(
                        Arrays.asList(new VarInitStatement(Type.STRING, Sym.of("expression"), new StrLitExpr("a"), Span.dummySpan()),
                                new VarInitStatement(Type.OBJECT, Sym.of("expr"), new InvokeStaticExpr(
                                        new ClassType(ognl.Ognl.class),
                                        Arrays.asList(),
                                        "getValue",
                                        Arrays.asList(Type.STRING, Type.MAP, Type.OBJECT),
                                        Arrays.asList(Sym.of("expression"),
                                                Sym.of("env"),
                                                Sym.of("env")),
                                        Type.OBJECT),
                                        Span.dummySpan()),
                                new VarInitStatement(Type.OBJECT, Sym.of("tmp"), new VarExpr(Sym.of("expr")), Span.dummySpan()),
                                new VarInitStatement(Type.THROWABLE, Sym.of("actual"), new NullExpr(), Span.dummySpan())),
                        Arrays.asList(new CatchClause(
                                        new ClassType(ognl.OgnlException.class),
                                        Sym.of("ex2"),
                                        Arrays.asList(new VarInitStatement(
                                                        Type.THROWABLE,
                                                        Sym.of("reason"),
                                                        new InvokeExpr(
                                                                new ClassType(ognl.OgnlException.class),
                                                                Sym.of("ex2"),
                                                                "getReason",
                                                                Arrays.asList(),
                                                                Arrays.asList(),
                                                                Type.THROWABLE),
                                                        Span.dummySpan()),
                                                new VarInitStatement(
                                                        Type.THROWABLE,
                                                        Sym.of("nullValue"),
                                                        new NullExpr(),
                                                        Span.dummySpan()),
                                                new VarInitStatement(
                                                        Type.BOOLEAN,
                                                        Sym.of("cond"),
                                                        new EqualOpExpr(Sym.of("reason"), Sym.of("nullValue")),
                                                        Span.dummySpan()),
                                                new IfStatement(
                                                        Sym.of("cond"),
                                                        Arrays.asList(
                                                                new VarInitStatement(
                                                                        Type.THROWABLE,
                                                                        Sym.of("actual"),
                                                                        new VarExpr(Sym.of("ex2")),
                                                                        Span.dummySpan())),
                                                        Arrays.asList(
                                                                new VarInitStatement(
                                                                        Type.THROWABLE,
                                                                        Sym.of("actual"),
                                                                        new VarExpr(Sym.of("reason")),
                                                                        Span.dummySpan()))))),
                                new CatchClause(
                                        new ClassType(java.lang.Throwable.class),
                                        Sym.of("ex"),
                                        Arrays.asList(
                                                new VarInitStatement(
                                                        Type.THROWABLE, Sym.of("actual"), new VarExpr(Sym.of("ex")), Span.dummySpan())))
                        ),
                        Arrays.asList()),
                new VarInitStatement(MATCHER, Sym.of("expected"), new InstanceOfMatcherExpr("java.lang.String"), Span.dummySpan()),
                new IsStatement(Sym.of("actual"), Sym.of("expected"), Span.dummySpan()));
        assertThat(actual, is(expected));
    }
    
}
