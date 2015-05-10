package yokohama.unit.translator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import yokohama.unit.ast.ClassCheckVisitor;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.VariableCheckVisitor;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.Either;
import yokohama.unit.util.GenSym;

@RequiredArgsConstructor
class ErrorCollector {
    final Group group;
    final Stream<ErrorMessage> errors;

    public ErrorCollector append(Function<Group, List<ErrorMessage>> f) {
        return new ErrorCollector(
                group, Stream.concat(errors, f.apply(group).stream()));
    }

    public List<ErrorMessage> getErrors() {
        return errors.collect(Collectors.toList());
    }

    public static ErrorCollector of(Group group) {
        return new ErrorCollector(group, Stream.empty());
    }
}

@AllArgsConstructor
public class DocyCompilerImpl implements DocyCompiler {
    DocyParser docyParser;
    ParseTreeToAstVisitorFactory parseTreeToAstVisitorFactory;
    VariableCheckVisitor variableCheckVisitor;
    AstToJUnitAstFactory astToJUnitAstFactory;
    ExpressionStrategyFactory expressionStrategyFactory;
    MockStrategyFactory mockStrategyFactory;
    JUnitAstCompiler jUnitAstCompiler;

    @SneakyThrows(MalformedURLException.class)
    private URL toURL(String cp) {
        return new File(cp).toURI().toURL();
    }
    
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
        // Make a class loader
        ClassLoader classLoader = new URLClassLoader(
                classPath.stream().map(this::toURL).toArray(i -> new URL[i]),
                ClassLoader.getSystemClassLoader());

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
        List<ErrorMessage> errors =
                ErrorCollector.of(ast)
                        .append(variableCheckVisitor::check)
                        .getErrors();

        Either<List<ErrorMessage>, ClassResolver> classResolverOrErrors =
                new ClassCheckVisitor(classLoader).check(ast);
        List<ErrorMessage> classCheckErrors =
                classResolverOrErrors.leftOptional().orElse(Collections.emptyList());

        if (!errors.isEmpty() || !classCheckErrors.isEmpty()) {
            return ListUtils.union(errors, classCheckErrors);
        }

        ClassResolver classResolver = classResolverOrErrors.rightOptional().get();

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
                    genSym,
                    classResolver)
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
