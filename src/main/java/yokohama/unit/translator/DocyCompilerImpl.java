package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.VariableCheckVisitor;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;
import yokohama.unit.util.GenSym;

@AllArgsConstructor
public class DocyCompilerImpl implements DocyCompiler {
    DocyParser docyParser;
    ParseTreeToAstVisitorFactory parseTreeToAstVisitorFactory;
    VariableCheckVisitor variableCheckVisitor;
    AstToJUnitAstFactory astToJUnitAstFactory;
    ExpressionStrategyFactory expressionStrategyFactory;
    MockStrategyFactory mockStrategyFactory;
    JUnitAstCompiler jUnitAstCompiler;

    @Override
    public List<ErrorMessage> compile(
            Path docyPath,
            InputStream ins,
            String name,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            boolean emitJava,
            List<String> javacArgs) {

        // Source to ANTLR parse tree
        List<ErrorMessage> docyParserErrors = new ArrayList<>();
        GroupContext ctx;
        try {
            ctx = docyParser.parse(docyPath, ins, docyParserErrors);
        } catch (IOException e) {
            Span span = Span.of(docyPath);
            return Arrays.asList(new ErrorMessage(e.getMessage(), span));
        }
        if (!docyParserErrors.isEmpty()) return docyParserErrors;

        // ANTLR parse tree to AST
        Group ast = parseTreeToAstVisitorFactory.create(Optional.of(docyPath))
                .visitGroup(ctx);

        // Check AST
        List<ErrorMessage> variableCheckErrors = variableCheckVisitor.check(ast);
        if (!variableCheckErrors.isEmpty()) return variableCheckErrors;

        // AST to JUnit AST
        CompilationUnit junit;
        try {
            GenSym genSym = new GenSym();
            ExpressionStrategy expressionStrategy =
                    expressionStrategyFactory.create(name, packageName, genSym);
            MockStrategy mockStrategy =
                    mockStrategyFactory.create(name, packageName, genSym);
            junit = astToJUnitAstFactory.create(name,
                    packageName,
                    expressionStrategy,
                    mockStrategy,
                    genSym)
                    .translate(ast);
        } catch (TranslationException e) {
            return Arrays.asList(e.toErrorMessage());
        }

        if (emitJava) {
            Path javaFilePath 
                    = TranslatorUtils.makeClassFilePath(dest, packageName, name, ".java");
            try {
                FileUtils.write(javaFilePath.toFile(), junit.getText());
            } catch (IOException e) {
                Span span = Span.of(docyPath);
                return Arrays.asList(new ErrorMessage(e.getMessage(), span));
            }
        }

        // JUnit AST to Java code
        return jUnitAstCompiler.compile(docyPath,
                junit,
                name,
                packageName,
                classPath,
                dest,
                javacArgs);
    }
}
