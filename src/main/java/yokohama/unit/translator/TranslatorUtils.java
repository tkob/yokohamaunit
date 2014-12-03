package yokohama.unit.translator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import yokohama.unit.ast.Group;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.OgnlExpressionStrategy;
import yokohama.unit.grammar.YokohamaUnitLexer;
import yokohama.unit.grammar.YokohamaUnitParser;

public class TranslatorUtils {

    public static Group parseDocy(final String input) {
        return parseDocy(input, YokohamaUnitLexer.DEFAULT_MODE);
    }

    @SneakyThrows(IOException.class)
    static Group parseDocy(final String input, final int mode) {
        InputStream bais = new ByteArrayInputStream(input.getBytes());
        CharStream stream = new ANTLRInputStream(bais);
        Lexer lex = new YokohamaUnitLexer(stream);
        lex.mode(mode);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        YokohamaUnitParser parser = new YokohamaUnitParser(tokens);
        return new ParseTreeToAstVisitor().visitGroup(parser.group());
    }

    public static String docyToJava(
            final String docy,
            final String className,
            final String packageName) {
        // Source to AST
        Group ast = parseDocy(docy);

        // AST to JUnit AST
        CompilationUnit junit =
                new AstToJUnitAst().translate(className, ast, packageName);

        // JUnit AST to string
        return junit.getText(new OgnlExpressionStrategy());
    }

    public static boolean compileDocy(
            final String docy,
            final String className,
            final String packageName,
            final String... options) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        JavaFileObject source = new SimpleJavaFileObject(
                URI.create("string:///"
                        + packageName.replace('.','/') + "/" + className
                        + Kind.SOURCE.extension),
                Kind.SOURCE
        ) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return docyToJava(docy, className, packageName);
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
