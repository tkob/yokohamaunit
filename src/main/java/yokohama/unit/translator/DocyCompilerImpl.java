package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.VariableCheckVisitor;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;

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
    public boolean compile(
            Path docyPath,
            InputStream ins,
            String className,
            String packageName,
            List<String> classPath,
            Optional<Path> dest,
            boolean emitJava,
            List<String> javacArgs
    ) throws IOException {
        List<ErrorMessage> errors = new ArrayList<>();

        // Source to ANTLR parse tree
        GroupContext ctx = docyParser.parse(ins, errors);
        if (errors.size() > 0) return false;

        // ANTLR parse tree to AST
        Group ast = parseTreeToAstVisitorFactory.create(Optional.of(docyPath))
                .visitGroup(ctx);

        // Check AST
        List<yokohama.unit.ast.ErrorMessage> errorMessages = variableCheckVisitor.check(ast);
        if (errorMessages.size() > 0) {
            for (yokohama.unit.ast.ErrorMessage errorMessage : errorMessages) {
                System.err.println(errorMessage.getSpan() + ": " + errorMessage.getMessage());
            }
            return false;
        }

        // AST to JUnit AST
        CompilationUnit junit =
                astToJUnitAstFactory.create(
                        className,
                        packageName,
                        expressionStrategy,
                        mockStrategy)
                        .translate(className, ast, packageName);

        // JUnit AST to Java code
        return jUnitAstCompiler.compile(docyPath, junit, className, packageName, classPath, dest, javacArgs);
    }
}
