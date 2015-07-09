package yokohama.unit.translator;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.JUnit4;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import yokohama.unit.position.ErrorMessage;

@RunWith(Theories.class)
public class DocyCompilerImplTest {
    @AllArgsConstructor
    public static class Fixture {
        public final String docy;
        public final ExpressionStrategyFactory expressionStrategyFactory;
        public final CombinationStrategy combinationStrategy;
        public final long numMethods;
    }

    @DataPoints
    public static Fixture[] PARAMs = {
        new Fixture(
                "OgnlTestIs.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 1),
        new Fixture(
                "OgnlTestIsNot.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 1),
        new Fixture(
                "OgnlTestNull.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 2),
        new Fixture(
                "OgnlTestThrows.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 5),
        new Fixture(
                "OgnlTestMultiplePropositions.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 1),
        new Fixture(
                "OgnlTestMultipleAssertions.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 2),
        new Fixture(
                "OgnlTestBindings.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 1),
        new Fixture(
                "OgnlTestTable.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 6),
        new Fixture(
                "OgnlTestCSV.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(),  3),
        new Fixture(
                "OgnlTestExcel.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 3),
        new Fixture(
                "OgnlTestFourPhase.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 2),
        new Fixture(
                "OgnlTestFourPhaseWithTeardown.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 1),
        new Fixture(
                "OgnlTestStub.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 7),
        new Fixture(
                "OgnlTestStubVariations.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 1),
        new Fixture(
                "OgnlTestInstanceSuchThat.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 4),
        new Fixture(
                "OgnlTestImport.docy", new OgnlExpressionStrategyFactory(), new CombinationStrategyImpl(), 4),
        new Fixture(
                "ElTestNull.docy", new ElExpressionStrategyFactory(), new CombinationStrategyImpl(), 2),
        new Fixture(
                "ElTestThrows.docy", new ElExpressionStrategyFactory(), new CombinationStrategyImpl(), 5),
        new Fixture(
                "ElTestImport.docy", new ElExpressionStrategyFactory(), new CombinationStrategyImpl(), 4),
        new Fixture(
                "ScalaTestNull.docy", new ScalaExpressionStrategyFactory(), new CombinationStrategyImpl(), 2),
        new Fixture(
                "ScalaTestThrows.docy", new ScalaExpressionStrategyFactory(), new CombinationStrategyImpl(), 5),
        new Fixture(
                "ScalaTestImport.docy", new ScalaExpressionStrategyFactory(), new CombinationStrategyImpl(), 4),
        new Fixture(
                "GroovyTestNull.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 2),
        new Fixture(
                "GroovyTestThrows.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 5),
        new Fixture(
                "GroovyTestImport.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 4),
        new Fixture(
                "GroovyTestLiterals.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 54),
        new Fixture(
                "GroovyTestInvocation.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 15),
        new Fixture(
                "GroovyTestCodeBlock.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 7),
        new Fixture(
                "GroovyTestInvoke.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 2),
        new Fixture(
                "GroovyTestCombination.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 17),
        new Fixture(
                "GroovyTestComment.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 1),
        new Fixture(
                "GroovyTestRegExp.docy", new GroovyExpressionStrategyFactory(), new CombinationStrategyImpl(), 7),
    };

    @DataPoints
    public static JUnitAstCompiler[] jUnitAstCompilers = {
        new JUnitAstCompilerImpl(),
        new BcelJUnitAstCompiler(),
    };

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    public static class FailurePrintListener extends RunListener {
        @Override
        public void testFailure(Failure failure) {
            System.err.println(failure.getTestHeader());
            System.err.println(failure.getTrace());
        }
    };

    @Theory
    public void testCompile(final Fixture fixture, final JUnitAstCompiler jUnitAstCompiler) throws Exception {
        try (InputStream ins = getClass().getResourceAsStream(fixture.docy)) {
            String className = FilenameUtils.removeExtension(fixture.docy);
            String packageName = "yokohama.unit.translator";
            File dest = temporaryFolder.getRoot();

            // test if fixture does compile
            {
                Path docyPath = Paths.get(fixture.docy);
                DocyCompiler instance = new DocyCompilerImpl(
                        new DocyParserImpl(),
                        new ParseTreeToAstVisitorFactory(),
                        new AstToJUnitAstFactory(),
                        fixture.expressionStrategyFactory,
                        new MockitoMockStrategyFactory(),
                        fixture.combinationStrategy,
                        jUnitAstCompiler);
                List<ErrorMessage> actual = instance.compile(
                        docyPath,
                        ins,
                        className,
                        packageName,
                        Arrays.asList(),
                        Optional.of(Paths.get(dest.getAbsolutePath())),
                        false,
                        Arrays.asList());
                List<ErrorMessage> expected = Collections.emptyList();
                assertThat(actual, is(expected));
            }

            // going to test compiled tests pass...

            // load compiled class
            URL urls[] = { dest.toURI().toURL() };
            ClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
            Class<?> klass = Class.forName(packageName + "." + className, true, loader);

            // see if there are expected number of methods
            {
                JUnit4 runner = new JUnit4(klass);
                long actual =
                        runner.getTestClass().getAnnotatedMethods(Test.class)
                                .stream().count();
                long expected = fixture.numMethods;
                assertThat(actual, is(expected));
            }

            // run the tests
            JUnitCore junit = new JUnitCore();
            RunListener listener = new FailurePrintListener();
            RunListener listenerSpy = spy(listener);
            junit.addListener(listenerSpy);
            junit.run(klass);

            // verify test run
            verify(listenerSpy, atLeastOnce()).testStarted(anyObject());
            verify(listenerSpy, never()).testFailure(anyObject());
            verify(listenerSpy, never()).testAssumptionFailure(anyObject());
            verify(listenerSpy, never()).testIgnored(anyObject());
            verify(listenerSpy, atLeastOnce()).testFinished(anyObject());
        }
    }
}
