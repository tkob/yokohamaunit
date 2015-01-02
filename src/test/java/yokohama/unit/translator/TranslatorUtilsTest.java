package yokohama.unit.translator;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
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

public class TranslatorUtilsTest {

    @RunWith(Theories.class)
    public static class DocyToJava {
        @DataPoints
        public static Fixture[] PARAMs = {
            new Fixture("TestIs.docy", "TestIs.java", Arrays.asList("Simple_Arithmetic_1")),
            new Fixture("TestThrows.docy", "TestThrows.java", Arrays.asList("Division_by_zero_1")),
            new Fixture("TestMultiplePropositions.docy", "TestMultiplePropositions.java", Arrays.asList("Multiple_propositions_1")),
            new Fixture(
                    "TestMultipleAssertions.docy",
                    "TestMultipleAssertions.java",
                    Arrays.asList("Multiple_assertions_1", "Multiple_assertions_2")
            ),
            new Fixture(
                    "TestBindings.docy",
                    "TestBindings.java",
                    Arrays.asList("String_startsWith_returns_true_if_the_prefix_is_empty_1")
            ),
            new Fixture(
                    "TestTable.docy",
                    "TestTable.java",
                    Arrays.asList("String_startsWith_1_1", "String_startsWith_1_2", "String_startsWith_1_3")
            ),
            new Fixture(
                    "TestCSV.docy",
                    "TestCSV.java",
                    Arrays.asList("String_startsWith_1_1", "String_startsWith_1_2", "String_startsWith_1_3")
            ),
            new Fixture(
                    "TestExcel.docy",
                    "TestExcel.java",
                    Arrays.asList("String_startsWith_1_1", "String_startsWith_1_2", "String_startsWith_1_3")
            ),
            new Fixture(
                    "TestFourPhase.docy",
                    "TestFourPhase.java",
                    Arrays.asList("AtomicInteger_incrementAndGet_increments_the_content")
            ),
            new Fixture(
                    "TestFourPhaseWithTeardown.docy",
                    "TestFourPhaseWithTeardown.java",
                    Arrays.asList("The_size_of_a_new_temporary_file_is_zero")
            ),
            new Fixture(
                    "TestStub.docy",
                    "TestStub.java",
                    Arrays.asList(
                            "Submit_a_task_and_get_the_result_1",
                            "Collections_unmodifiableMap_preserves_lookup_1",
                            "StringBuilder_append_CharSequence_int_int_calls_CharSequence_charAt"
                    )
            ),
            new Fixture(
                    "TestStubVariations.docy",
                    null,
                    Arrays.asList(
                            "Variations_of_stubbing_1"
                    )
            ),
        };

        @Theory
        public void testDocyToJava(final Fixture fixture) throws Exception {
            assumeThat(fixture.java, is(not(nullValue())));
            try (   InputStream docyIn = getClass().getResourceAsStream(fixture.docy);
                    InputStream javaIn = getClass().getResourceAsStream(fixture.java)) {
                String docy = IOUtils.toString(docyIn, "UTF-8");
                String className = FilenameUtils.removeExtension(fixture.docy);
                String packageName = "yokohama.unit.translator";
                String actual = TranslatorUtils.docyToJava(Optional.empty(), docy, className, packageName);
                String expected = IOUtils.toString(javaIn, "UTF-8").replace("\r\n", IOUtils.LINE_SEPARATOR);
                assertThat(actual, is(expected));
            }
        }

        @Rule
        public TemporaryFolder temporaryFolder = new TemporaryFolder();

        @Theory
        public void testCompileDocy(final Fixture fixture) throws Exception {
            try (InputStream docyIn = getClass().getResourceAsStream(fixture.docy)) {
                String docy = IOUtils.toString(docyIn, "UTF-8");
                String className = FilenameUtils.removeExtension(fixture.docy);
                String packageName = "yokohama.unit.translator";
                File dest = temporaryFolder.getRoot();

                {
                    boolean actual =
                            TranslatorUtils.compileDocy(
                                    Optional.empty(),
                                    docy,
                                    className, 
                                    packageName,
                                    "-d", dest.getAbsolutePath());
                    boolean expected = true;
                    assertThat(actual, is(expected));
                }

                URL urls[] = { dest.toURI().toURL() };
                ClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
                Class<?> klass = Class.forName(packageName + "." + className, true, loader);

                long numTestMethods = Arrays.asList(klass.getDeclaredMethods())
                        .stream()
                        .filter(method -> method.getAnnotation(Test.class) != null)
                        .count();
                assumeThat((int)numTestMethods, is(greaterThan(0))); // otherwise `new JUnit4` would fail

                JUnit4 runner = new JUnit4(klass);

                {
                    Set<String> actual =
                            runner.getTestClass().getAnnotatedMethods(Test.class)
                                    .stream()
                                    .map(FrameworkMethod::getName)
                                    .collect(Collectors.toSet());
                    Set<String> expected = new TreeSet<String>(fixture.methods);
                    assertThat(actual, is(expected));
                }

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

                verify(listenerSpy, atLeastOnce()).testStarted(anyObject());
                verify(listenerSpy, never()).testFailure(anyObject());
                verify(listenerSpy, never()).testAssumptionFailure(anyObject());
                verify(listenerSpy, never()).testIgnored(anyObject());
                verify(listenerSpy, atLeastOnce()).testFinished(anyObject());
            }
        }

        @AllArgsConstructor
        public static class Fixture {
            public final String docy;
            public final String java;
            public final List<String> methods;
        }
    }

}
