package yokohama.unit.translator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
            final Optional<Path> docyPath,
            final String docy,
            final String className,
            final String packageName) {
        // Source to AST
        Group ast = parseDocy(docy);

        // AST to JUnit AST
        CompilationUnit junit =
                new AstToJUnitAst(
                        docyPath,
                        className,
                        packageName,
                        new OgnlExpressionStrategy(),
                        new MockitoMockStrategy()
                ).translate(className, ast, packageName);

        // JUnit AST to string
        return junit.getText(
                new yokohama.unit.ast_junit.OgnlExpressionStrategy(),
                new yokohama.unit.ast_junit.MockitoMockStrategy());
    }

    public static boolean compileDocy(
            final Optional<Path> path,
            final String docy,
            final String className,
            final String packageName,
            final List<String> classPath,
            final Optional<Path> dest,
            final String... options) throws IOException {
        return new DocyCompilerImpl().compile(
                path.get(),
                new ByteArrayInputStream(docy.getBytes()),
                className,
                packageName,
                classPath,
                dest,
                Arrays.asList(options));
    }
}
