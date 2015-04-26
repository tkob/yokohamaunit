package yokohama.unit.translator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.apache.commons.io.FilenameUtils;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Position;
import yokohama.unit.position.Span;

public class JUnitAstCompilerImpl implements JUnitAstCompiler {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    @Override
    public List<ErrorMessage> compile(
            Path docyPath,
            CompilationUnit ast,
            String name,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            List<String> javacArgs) {
        String javaCode = ast.getText();

        // Compile Java code
        if (compiler == null) {
            ErrorMessage errorMessage = new ErrorMessage(
                    "Could not get the system Java compiler. " +
                    "Probably either JAVA_HOME variable is not set or " +
                    "it does not point to JDK directory.",
                    Span.dummySpan());
            return Arrays.asList(errorMessage);
        }

        List<String> args = new ArrayList<>();
        if (classPath.size() > 0) {
            args.add("-cp");
            args.add(String.join(File.pathSeparator, classPath));
        }
        if (dest.isPresent()) {
            args.add("-d");
            args.add(dest.get().toString());
        }
        args.addAll(javacArgs);

        List<ErrorMessage> errors = new ArrayList<>();
        DiagnosticListener<JavaFileObject> diagnosticListener = new DiagnosticListener<JavaFileObject>() {
            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                if (diagnostic.getKind() != Diagnostic.Kind.ERROR) return;
                Path path = Paths.get(diagnostic.getSource().toUri());
                int line = (int)diagnostic.getLineNumber();
                int column = (int)diagnostic.getColumnNumber();
                Span span = Span.of(path, Position.of(line, column));
                ErrorMessage errorMessage = new ErrorMessage(diagnostic.getMessage(null), span);
                errors.add(errorMessage);
            }
        };

        CompilationTask task = compiler.getTask(null, /* Writer out */
                null, /* JavaFileManager fileManager */
                diagnosticListener,
                args,
                null, /* Iterable<String> classes */
                Arrays.asList(new SimpleJavaFileObject(
                        Paths.get(FilenameUtils.removeExtension(docyPath.toString()) + ".java").toUri(),
                        Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                        return javaCode;
                    }
                }));
        task.call(); 
        return errors;
    }
}
