package yokohama.unit.translator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.commons.io.IOUtils;
import yokohama.unit.annotations.As;
import yokohama.unit.annotations.GroovyAs;
import yokohama.unit.ast.Group;
import yokohama.unit.grammar.YokohamaUnitLexer;
import yokohama.unit.grammar.YokohamaUnitParser;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;

@As
public class DataConverters {
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

    @As
    @SneakyThrows(IOException.class)
    public static Group asAst(String input) {
        ErrorListener errorListener = new ErrorListener();
        InputStream bais = new ByteArrayInputStream(input.getBytes());
        CharStream stream = new ANTLRInputStream(bais);
        Lexer lex = new YokohamaUnitLexer(stream);
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

    @GroovyAs
    @SneakyThrows(IOException.class)
    public static String inputStreamAsString(InputStream is, Class<?> clazz) {
        return IOUtils.toString(is, "UTF-8");
    }

    // dummy conversion methods for test

    public static String noAnnotation(InputStream is, Class<?> clazz) {
        throw new UnsupportedOperationException("shoud not use this method");
    }

    @GroovyAs
    public static String onlyOneArgument(InputStream is) {
        throw new UnsupportedOperationException("shoud not use this method");
    }

    @GroovyAs
    public static String secondArgumentNotClass(InputStream is, String s) {
        throw new UnsupportedOperationException("shoud not use this method");
    }
}
