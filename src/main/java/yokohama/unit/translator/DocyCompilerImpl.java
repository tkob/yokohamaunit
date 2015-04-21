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

@AllArgsConstructor
public class DocyCompilerImpl implements DocyCompiler {
    DocyParser docyParser;
    ParseTreeToAstVisitorFactory parseTreeToAstVisitorFactory;
    VariableCheckVisitor variableCheckVisitor;
    AstToJUnitAstFactory astToJUnitAstFactory;
    ExpressionStrategy expressionStrategy;
    MockStrategy mockStrategy;
    JUnitAstCompiler jUnitAstCompiler;

    @Override
    public List<ErrorMessage> compile(
            Path docyPath,
            InputStream ins,
            String className,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            boolean emitJava,
            List<String> javacArgs
    ) throws IOException {

        // Source to ANTLR parse tree
        List<ErrorMessage> docyParserErrors = new ArrayList<>();
        GroupContext ctx = docyParser.parse(docyPath, ins, docyParserErrors);
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
            junit = astToJUnitAstFactory.create(
                    className,
                    packageName,
                    expressionStrategy,
                    mockStrategy)
                    .translate(className, ast, packageName);
        } catch (TranslationException e) {
            return Arrays.asList(e.toErrorMessage());
        }

        if (emitJava) {
            Path javaFilePath 
                    = TranslatorUtils.makeClassFilePath(dest, packageName, className, ".java");
            FileUtils.write(javaFilePath.toFile(), junit.getText());
        }

        // JUnit AST to Java code
        return jUnitAstCompiler.compile(
                docyPath,
                junit,
                className,
                packageName,
                classPath,
                dest,
                javacArgs);
    }
}
