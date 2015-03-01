package yokohama.unit.translator;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.ExpressionStrategy;
import yokohama.unit.ast_junit.MockStrategy;

public class JUnitAstCompilerImpl implements JUnitAstCompiler {
    ExpressionStrategy expressionStrategy;
    MockStrategy mockStrategy;

    public JUnitAstCompilerImpl(
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy) {
        this.expressionStrategy = expressionStrategy;
        this.mockStrategy = mockStrategy;
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    @Override
    public boolean compile(
            CompilationUnit ast,
            String className,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            List<String> javacArgs) {
        String javaCode = ast.getText(expressionStrategy, mockStrategy);

        // Compile Java code
        if (compiler == null) {
            System.err.println("Could not get the system Java compiler. Probably either JAVA_HOME variable is not set or it does not point to JDK directory.");
            return false;
        }

        List<String> args = new ArrayList<>();
        if (classPath.size() > 0) {
            args.add("-cp");
            args.add(String.join(File.pathSeparator, args));
        }
        if (dest.isPresent()) {
            args.add("-d");
            args.add(dest.get().toString());
        }
        args.addAll(javacArgs);

        CompilationTask task = compiler.getTask(
                null, /* Writer out */
                null, /* JavaFileManager fileManager */
                null, /* DiagnosticListener<? super JavaFileObject> diagnosticListener */
                args,
                null, /* Iterable<String> classes */
                Arrays.asList(new SimpleJavaFileObject(
                        URI.create("string:///"
                                + packageName.replace('.','/') + "/" + className
                                + Kind.SOURCE.extension),
                        Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                        return javaCode;
                    }
                }));
        return task.call(); 
    }
}
