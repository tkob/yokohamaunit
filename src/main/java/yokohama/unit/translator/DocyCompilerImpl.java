package yokohama.unit.translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.VariableCheckVisitor;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.ast_junit.ExpressionStrategy;
import yokohama.unit.ast_junit.MockStrategy;
import yokohama.unit.ast_junit.MockitoMockStrategy;
import yokohama.unit.ast_junit.OgnlExpressionStrategy;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;

public class DocyCompilerImpl implements DocyCompiler {
    DocyParser docyParser = new DocyParserImpl();
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
        List<ErrorMessage> errors = new ArrayList<>();

        // Source to ANTLR parse tree
        GroupContext ctx = docyParser.parse(ins, errors);
        if (errors.size() > 0) return false;

        // ANTLR parse tree to AST
        Group ast = parseTreeToAstVisitor.visitGroup(ctx);

        // Check AST
        List<yokohama.unit.ast.ErrorMessage> errorMessages = variableCheckVisitor.check(ast);
        if (errorMessages.size() > 0) {
            for (yokohama.unit.ast.ErrorMessage errorMessage : errorMessages) {
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
