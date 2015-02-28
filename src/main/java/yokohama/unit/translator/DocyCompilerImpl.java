package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import yokohama.unit.ast.ErrorMessage;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.VariableCheckVisitor;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.ExpressionStrategy;
import yokohama.unit.ast_junit.MockStrategy;
import yokohama.unit.ast_junit.MockitoMockStrategy;
import yokohama.unit.ast_junit.OgnlExpressionStrategy;
import yokohama.unit.grammar.YokohamaUnitLexer;
import yokohama.unit.grammar.YokohamaUnitParser;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;

public class DocyCompilerImpl implements DocyCompiler {
    ParseTreeToAstVisitor parseTreeToAstVisitor = new ParseTreeToAstVisitor();
    VariableCheckVisitor variableCheckVisitor = new VariableCheckVisitor();
    AstToJUnitAstFactory astToJUnitAstFactory = new AstToJUnitAstFactory();
    ExpressionStrategy expressionStrategy = new OgnlExpressionStrategy();
    MockStrategy mockStrategy = new MockitoMockStrategy();
    JUnitAstCompiler jUnitAstCompiler = new JUnitAstCompilerImpl(expressionStrategy, mockStrategy);

    @Override
    public boolean compile(
            Path docyPath,
            InputStream ins,
            String className,
            String packageName,
            List<String> javacArgs
    ) throws IOException {
        // Source to ANTLR parse tree
        final AtomicInteger numErrors = new AtomicInteger(0);
        BaseErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(
                    Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int line,
                    int charPositionInLine,
                    String msg,
                    RecognitionException e) {
                numErrors.incrementAndGet();
            }
        };
        CharStream stream = new ANTLRInputStream(ins);
        Lexer lex = new YokohamaUnitLexer(stream);
        lex.addErrorListener(errorListener);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        YokohamaUnitParser parser = new YokohamaUnitParser(tokens);
        parser.addErrorListener(errorListener);

        GroupContext ctx = parser.group();
        if (numErrors.get() > 0) return false;

        // ANTLR parse tree to AST
        Group ast = parseTreeToAstVisitor.visitGroup(ctx);

        // Check AST
        List<ErrorMessage> errorMessages = variableCheckVisitor.check(ast);
        if (errorMessages.size() > 0) {
            for (ErrorMessage errorMessage : errorMessages) {
                System.err.println(errorMessage.getSpan() + ": " + errorMessage.getMessage());
            }
            return false;
        }

        // AST to JUnit AST
        CompilationUnit junit = astToJUnitAstFactory.create(Optional.of(docyPath))
                .translate(className, ast, packageName);

        // JUnit AST to Java code
        return jUnitAstCompiler.compile(junit, className, packageName, javacArgs);
    }
}
