package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import yokohama.unit.grammar.YokohamaUnitLexer;
import yokohama.unit.grammar.YokohamaUnitParser;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Position;
import yokohama.unit.position.Span;

public class DocyParserImpl implements DocyParser {

    @Override
    public YokohamaUnitParser.GroupContext parse(
            Path docyPath,
            InputStream ins,
            List<? super ErrorMessage> errors)
            throws IOException {
        BaseErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(
                    Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int line,
                    int charPositionInLine,
                    String msg,
                    RecognitionException e) {
                Span span = Span.of(
                        docyPath,
                        Position.of(line, charPositionInLine + 1));
                errors.add(new ErrorMessage(msg, span));
            }
        };
        CharStream stream = new ANTLRInputStream(ins);
        Lexer lex = new YokohamaUnitLexer(stream);
        lex.removeErrorListeners();
        lex.addErrorListener(errorListener);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        YokohamaUnitParser parser = new YokohamaUnitParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        GroupContext ctx = parser.group();
        return ctx;
    }
    
}
