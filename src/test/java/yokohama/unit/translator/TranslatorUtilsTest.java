package yokohama.unit.translator;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class TranslatorUtilsTest {

    @RunWith(Theories.class)
    public static class DocyToJava {
        @DataPoints
        public static Fixture[] PARAMs = {
            new Fixture("TestBlank.docy", "TestBlank.java", Arrays.asList()),
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
        };

        @Theory
        public void testDocyToJava(final Fixture fixture) throws Exception {
            try (   InputStream docyIn = getClass().getResourceAsStream(fixture.docy);
                    InputStream javaIn = getClass().getResourceAsStream(fixture.java)) {
                String docy = IOUtils.toString(docyIn, "UTF-8");
                String className = FilenameUtils.removeExtension(fixture.docy);
                String packageName = "yokohama.unit.translator";
                String actual = TranslatorUtils.docyToJava(docy, className, packageName);
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

                RunNotifier notifier = mock(RunNotifier.class);
                runner.run(notifier);

                verify(notifier, atLeastOnce()).fireTestStarted(anyObject());
                verify(notifier, never()).fireTestFailure(anyObject());
                verify(notifier, never()).fireTestAssumptionFailed(anyObject());
                verify(notifier, never()).fireTestIgnored(anyObject());
                verify(notifier, atLeastOnce()).fireTestFinished(anyObject());
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
