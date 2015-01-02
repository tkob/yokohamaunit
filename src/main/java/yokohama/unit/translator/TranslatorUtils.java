package yokohama.unit.translator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import yokohama.unit.ast.Group;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.MockitoMockStrategy;
import yokohama.unit.ast_junit.OgnlExpressionStrategy;
import yokohama.unit.grammar.YokohamaUnitLexer;
import yokohama.unit.grammar.YokohamaUnitParser;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;

public class TranslatorUtils {

    public static class TranslationException extends RuntimeException {
    }

    private static class ErrorListener extends BaseErrorListener {
        public int numErrors = 0;
        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
            numErrors++;
        }
    }

    public static Group parseDocy(final String input) {
        return parseDocy(input, YokohamaUnitLexer.DEFAULT_MODE);
    }

    @SneakyThrows(IOException.class)
    static Group parseDocy(final String input, final int mode) {
        ErrorListener errorListener = new ErrorListener();
        InputStream bais = new ByteArrayInputStream(input.getBytes());
        CharStream stream = new ANTLRInputStream(bais);
        Lexer lex = new YokohamaUnitLexer(stream);
        lex.mode(mode);
        lex.addErrorListener(errorListener);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        YokohamaUnitParser parser = new YokohamaUnitParser(tokens);
        parser.addErrorListener(errorListener);
        GroupContext ctx = parser.group();
        if (errorListener.numErrors > 0) {
            throw new TranslationException();
        }
        return new ParseTreeToAstVisitor().visitGroup(ctx);
    }

    public static String docyToJava(
            final Optional<Path> path,
            final String docy,
            final String className,
            final String packageName) {
        // Source to AST
        Group ast = parseDocy(docy);

        // AST to JUnit AST
        CompilationUnit junit =
                new AstToJUnitAst().translate(className, ast, packageName);

        // JUnit AST to string
        return junit.getText(new OgnlExpressionStrategy(), new MockitoMockStrategy());
    }

    public static boolean compileDocy(
            final Optional<Path> path,
            final String docy,
            final String className,
            final String packageName,
            final String... options) {
        final URI uri = path.map(Path::toUri).orElseGet(() ->
                URI.create("string:///"
                        + packageName.replace('.','/') + "/" + className
                        + Kind.SOURCE.extension));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        JavaFileObject source = new SimpleJavaFileObject(uri, Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return docyToJava(path, docy, className, packageName);
            }
        };

        CompilationTask task = compiler.getTask(
                null, /* Writer out */
                null, /* JavaFileManager fileManager */
                null, /* DiagnosticListener<? super JavaFileObject> diagnosticListener */
                Arrays.asList(options),
                null, /* Iterable<String> classes */
                Arrays.asList(source)
        );
        return task.call(); 
    }
}
