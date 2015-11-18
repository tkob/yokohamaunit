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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import yokohama.unit.ast.Abbreviation;
import yokohama.unit.ast.AnchorCheckVisitor;
import yokohama.unit.ast.AnyOfCheckVisitor;
import yokohama.unit.ast.ClassCheckVisitor;
import yokohama.unit.ast.Group;
import yokohama.unit.ast.RegExpCheckVisitor;
import yokohama.unit.ast.VariableCheckVisitor;
import yokohama.unit.ast_junit.CompilationUnit;
import yokohama.unit.grammar.YokohamaUnitParser.GroupContext;
import yokohama.unit.position.ErrorMessage;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.Either;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Pair;

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

@RequiredArgsConstructor
public class DocyCompilerImpl implements DocyCompiler {
    final DocyParser docyParser;
    final ParseTreeToAstVisitorFactory parseTreeToAstVisitorFactory;
    final AstToJUnitAstFactory astToJUnitAstFactory;
    final ExpressionStrategyFactory expressionStrategyFactory;
    final MockStrategyFactory mockStrategyFactory;
    final CombinationStrategy combinationStrategy;
    final JUnitAstCompiler jUnitAstCompiler;
    
    final VariableCheckVisitor variableCheckVisitor = new VariableCheckVisitor();
    final AnchorCheckVisitor anchorCheckVisitor = new AnchorCheckVisitor();

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
            boolean checkContract,
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
        // Create ClassResolver and get errors if any
        Pair<ClassResolver, List<ErrorMessage>> classResolverAndErrors =
                createClassResolver(ast.getAbbreviations(), classLoader);
        ClassResolver classResolver = classResolverAndErrors.getFirst();
        List<ErrorMessage> classResolverErrors = classResolverAndErrors.getSecond();
        ClassCheckVisitor classCheckVisitor = new ClassCheckVisitor(classResolver);
        AnyOfCheckVisitor anyOfCheckVisitor = new AnyOfCheckVisitor();
        RegExpCheckVisitor regExpCheckVisitor = new RegExpCheckVisitor();
        
        // other AST checks
        List<ErrorMessage> errors =
                ErrorCollector.of(ast)
                        .append(__ -> classResolverErrors)
                        .append(classCheckVisitor::check)
                        .append(variableCheckVisitor::check)
                        .append(anchorCheckVisitor::check)
                        .append(anyOfCheckVisitor::check)
                        .append(regExpCheckVisitor::check)
                        .getErrors();
        if (!errors.isEmpty()) return errors;

        // AST to JUnit AST
        CompilationUnit junit;
        try {
            GenSym genSym = new GenSym();
            ExpressionStrategy expressionStrategy =
                    expressionStrategyFactory.create(
                            name, packageName, genSym, classResolver);
            MockStrategy mockStrategy =
                    mockStrategyFactory.create(
                            name, packageName, genSym, classResolver);
            junit = astToJUnitAstFactory.create(name,
                    packageName,
                    expressionStrategy,
                    mockStrategy,
                    combinationStrategy,
                    genSym,
                    classResolver,
                    checkContract)
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

    private Pair<ClassResolver, List<ErrorMessage>> createClassResolver(
            List<Abbreviation> abbreviations, ClassLoader classLoader) {
        List<Either<ErrorMessage, Pair<String, String>>> bindingsOrErrors =
                abbreviations.stream().map(abbreviation -> {
                    String longName = abbreviation.getLongName();
                    Span span = abbreviation.getSpan();
                    try {
                        Class.forName(longName, false, classLoader);
                    } catch (ClassNotFoundException e) {
                        return Either.<ErrorMessage, Pair<String, String>>left(
                                new ErrorMessage("cannot find class: " + longName, span));
                    }
                    return Either.<ErrorMessage, Pair<String, String>>right(abbreviation.toPair());
                }).collect(Collectors.toList());
        Stream<Pair<String, String>> bindings =
                bindingsOrErrors.stream().flatMap(Either::rightStream);
        List<ErrorMessage> errors =
                bindingsOrErrors.stream()
                        .flatMap(Either::leftStream)
                        .collect(Collectors.toList());
        return Pair.of(new ClassResolver(bindings, classLoader), errors);
    }
}
