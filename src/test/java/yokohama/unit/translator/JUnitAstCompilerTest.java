package yokohama.unit.translator;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.AllArgsConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import yokohama.unit.ast_junit.ArrayExpr;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.Method;
import yokohama.unit.ast_junit.ReturnStatement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;

@RunWith(Theories.class)
public class JUnitAstCompilerTest {
    @AllArgsConstructor
    public static class Fixture {
        public final String name;
        public final CompilationUnit ast;
        public final Object expected;
    }

    @DataPoints
    public static Fixture[] PARAMs = {
        new Fixture(
                "ArrayCreation",
                new CompilationUnit(
                        "yokohama.unit.translator",
                        Arrays.asList(
                                new ClassDecl(
                                        true,
                                        "ArrayCreation",
                                        Optional.empty(),
                                        Arrays.asList(
                                                new ClassType(
                                                        java.util.concurrent.Callable.class)),
                                        Arrays.asList(
                                                new Method(
                                                        Collections.emptyList(),
                                                        "call",
                                                        Collections.emptyList(),
                                                        Optional.of(Type.OBJECT),
                                                        Arrays.asList(ClassType.EXCEPTION),
                                                        Arrays.asList(
                                                                new VarInitStatement(
                                                                        Type.STRING,
                                                                        "content1",
                                                                        new StrLitExpr("a"),
                                                                        Span.dummySpan()),
                                                                new VarInitStatement(
                                                                        Type.STRING,
                                                                        "content2",
                                                                        new StrLitExpr("b"),
                                                                        Span.dummySpan()),
                                                                new VarInitStatement(
                                                                        Type.STRING.toArray(),
                                                                        "ret",
                                                                        new ArrayExpr(
                                                                                Type.STRING.toArray(),
                                                                                Arrays.asList(
                                                                                        new Var("content1"),
                                                                                        new Var("content2"))),
                                                                        Span.dummySpan()),
                                                                new ReturnStatement(new Var("ret")))))))),
        new String[] { "a", "b" }),
    };

    @DataPoints
    public static JUnitAstCompiler[] jUnitAstCompilers = {
        new JUnitAstCompilerImpl(),
        new BcelJUnitAstCompiler(),
    };

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Theory
    public void testCompile(final Fixture fixture, final JUnitAstCompiler compiler) throws Exception {
        Path dest = temporaryFolder.getRoot().toPath();

        Path docyPath = Paths.get(fixture.name + ".docy");
        CompilationUnit ast = fixture.ast;
        String name = fixture.name;
        String packageName = "yokohama.unit.translator";
        List<String> classPath = Collections.emptyList();
        List<String> javacArgs = Collections.emptyList();
        {
            List<ErrorMessage> actual =
                    compiler.compile(
                            docyPath, ast, name, packageName, classPath, Optional.of(dest), javacArgs);
            List<ErrorMessage> expected = Collections.emptyList();
            assertThat(actual, is(expected)); 
        }

        // load compiled class
        URL urls[] = { dest.toUri().toURL() };
        ClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
        Class<?> klass = Class.forName(packageName + "." + name, true, loader);
        Callable callable = (Callable)klass.newInstance();
        {
            Object actual = callable.call();
            Object expected = fixture.expected;
            assertThat(actual, is(expected)); 
        }
    }
}
