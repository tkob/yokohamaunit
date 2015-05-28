package yokohama.unit.translator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import yokohama.unit.ast.Assertion;
import yokohama.unit.ast.Fixture;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.TableExtractVisitor;
import yokohama.unit.ast_junit.Annotation;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.Method;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.util.Sym;
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
        AstToJUnitAst instance = new AstToJUnitAst(
                name,
                packageName,
                new OgnlExpressionStrategy(name, packageName, genSym, new ClassResolver()),
                new MockitoMockStrategy("Name", "com.example", genSym, new ClassResolver()),
                new CombinationStrategyImpl(),
                genSym,
                new ClassResolver(),
                new TableExtractVisitor());
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
                        new CombinationStrategyImpl(),
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
                        new CombinationStrategyImpl(),
                        genSym,
                        new ClassResolver(),
                        Collections.emptyList(),
                        Collections.emptyMap());
        List<List<Statement>> actual = instance.translateAssertion(assertion, Sym.of("env"));
        List<List<Statement>> expected = Arrays.asList(Arrays.asList());
        assertThat(actual, is(expected));
    }
}
