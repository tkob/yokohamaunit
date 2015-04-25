package yokohama.unit.translator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import yokohama.unit.grammar.YokohamaUnitLexer;
import yokohama.unit.grammar.YokohamaUnitParser;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;

public class TranslatorUtils {
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
    public static Group parseDocy(final String input, final int mode) {
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
            throw new RuntimeException("Error while parsing docy");
        }
        return new ParseTreeToAstVisitor(Optional.empty()).visitGroup(ctx);
    }

    public static Path makeClassFilePath(
            Optional<Path> dest,
            String packageName,
            String className,
            String ext) {
        Path classFile = (dest.isPresent() ? dest.get(): Paths.get("."))
                .resolve(Paths.get(packageName.replace('.', '/')))
                .resolve(className + ext);
        return classFile;
    }
}
