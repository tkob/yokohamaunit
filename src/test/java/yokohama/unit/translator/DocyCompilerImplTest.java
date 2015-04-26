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
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
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
import org.junit.runners.model.FrameworkMethod;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import yokohama.unit.ast.VariableCheckVisitor;
import yokohama.unit.position.ErrorMessage;

@RunWith(Theories.class)
public class DocyCompilerImplTest {
    @AllArgsConstructor
    public static class Fixture {
        public final String docy;
        public final ExpressionStrategyFactory expressionStrategyFactory;
        public final List<String> methods;
    }

    @DataPoints
    public static Fixture[] PARAMs = {
        new Fixture(
                "TestIs.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("Simple_Arithmetic_1")),
        new Fixture(
                "TestIsNot.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("Simple_Arithmetic_1")),
        new Fixture(
                "TestNull.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("Null_test_1", "Null_test_2")),
        new Fixture(
                "TestThrows.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList(
                        "Division_by_zero_1",
                        "NPO_1",
                        "OgnlException_1",
                        "UnsupportedEncodingException_1",
                        "No_exception_1")),
        new Fixture(
                "TestMultiplePropositions.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("Multiple_propositions_1")),
        new Fixture(
                "TestMultipleAssertions.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("Multiple_assertions_1", "Multiple_assertions_2")),
        new Fixture(
                "TestBindings.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("String_startsWith_returns_true_if_the_prefix_is_empty_1")),
        new Fixture(
                "TestTable.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList(
                        "String_startsWith_1_1",
                        "String_startsWith_1_2",
                        "String_startsWith_1_3",
                        "String_endsWith_1_1",
                        "String_endsWith_1_2",
                        "String_endsWith_1_3")),
        new Fixture(
                "TestCSV.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList(
                        "String_startsWith_1_1",
                        "String_startsWith_1_2",
                        "String_startsWith_1_3")),
        new Fixture(
                "TestExcel.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList(
                        "String_startsWith_1_1",
                        "String_startsWith_1_2",
                        "String_startsWith_1_3")),
        new Fixture(
                "TestFourPhase.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("AtomicInteger_incrementAndGet_increments_the_content")),
        new Fixture(
                "TestFourPhaseWithTeardown.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("The_size_of_a_new_temporary_file_is_zero")),
        new Fixture(
                "TestStub.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList(
                        "Submit_a_task_and_get_the_result_1",
                        "Collections_unmodifiableMap_preserves_lookup_1",
                        "StringBuilder_append_CharSequence_int_int_calls_CharSequence_charAt")),
        new Fixture(
                "TestStubVariations.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList("Variations_of_stubbing_1")),
        new Fixture(
                "TestInstanceSuchThat.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList(
                        "instance_such_that_1",
                        "instance_such_that_2",
                        "instance_such_that_3",
                        "instance_such_that_4")),
        new Fixture(
                "TestImport.docy",
                new OgnlExpressionStrategyFactory(),
                Arrays.asList(
                        "Test_import_1",
                        "Test_import_2",
                        "Test_import_3",
                        "Test_import_4")),
        new Fixture(
                "ElTestNull.docy",
                new ElExpressionStrategyFactory(),
                Arrays.asList("Null_test_1", "Null_test_2")),
        new Fixture(
                "ElTestThrows.docy",
                new ElExpressionStrategyFactory(),
                Arrays.asList(
                        "Division_by_zero_does_not_throw_an_Exception_in_EL_1",
                        "Applying_null_to_a_method_does_not_throw_NPE_in_EL_1",
                        "ElException_1",
                        "IndexOutOfBoundsException_1",
                        "No_exception_1")),
        new Fixture(
                "ElTestImport.docy",
                new ElExpressionStrategyFactory(),
                Arrays.asList(
                        "Test_import_1",
                        "Test_import_2",
                        "Test_import_3",
                        "Test_import_4")),
    };

    @DataPoints
    public static JUnitAstCompiler[] jUnitAstCompilers = {
        new JUnitAstCompilerImpl(),
        new BcelJUnitAstCompiler(),
    };

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
                        new VariableCheckVisitor(),
                        new AstToJUnitAstFactory(),
                        fixture.expressionStrategyFactory,
                        new MockitoMockStrategyFactory(),
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

            // see if there are expected methods
            {
                JUnit4 runner = new JUnit4(klass);
                Set<String> actual =
                        runner.getTestClass().getAnnotatedMethods(Test.class)
                                .stream()
                                .map(FrameworkMethod::getName)
                                .collect(Collectors.toSet());
                Set<String> expected = new TreeSet<>(fixture.methods);
                assertThat(actual, is(expected));
            }

            // run the tests
            JUnitCore junit = new JUnitCore();
            RunListener listener = new RunListener() {
                @Override
                public void testFailure(Failure failure) {
                    System.err.println(failure.getTestHeader());
                    System.err.println(failure.getTrace());
                }
            };
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
