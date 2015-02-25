package yokohama.unit.translator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.commons.io.IOUtils;
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
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    @Override
    public boolean compile(
            Path docyPath,
            String className,
            String packageName,
            List<String> javacArgs
    ) throws IOException {
        // Get docy code
        String docyCode = IOUtils.toString(docyPath.toUri());

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
        InputStream ins = new FileInputStream(docyPath.toString());
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
        String javaCode = junit.getText(expressionStrategy, mockStrategy);

        // Compile Java code
        if (compiler == null) {
            System.err.println("Could not get the system Java compiler. Probably either JAVA_HOME variable is not set or it does not point to JDK directory.");
            return false;
        }

        CompilationTask task = compiler.getTask(
                null, /* Writer out */
                null, /* JavaFileManager fileManager */
                null, /* DiagnosticListener<? super JavaFileObject> diagnosticListener */
                javacArgs,
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
