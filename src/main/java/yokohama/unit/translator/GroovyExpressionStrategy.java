package yokohama.unit.translator;

import groovy.lang.GroovyShell;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast_junit.ArrayExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.SUtils;

@AllArgsConstructor
public class GroovyExpressionStrategy implements ExpressionStrategy {
    private final String name;
    private final String packageName;
    private final GenSym genSym;

    static final Type COMPILATION_CUSTOMIZER =
            new Type(new ClassType(CompilationCustomizer.class), 0);
    static final ClassType IMPORT_CUSTOMIZER =
            new ClassType(ImportCustomizer.class);
    static final ClassType COMPILER_CONFIGURATION =
            new ClassType(CompilerConfiguration.class);
    static final ClassType GROOVY_SHELL = new ClassType(GroovyShell.class);

    @Override
    public Collection<ClassDecl> auxClasses(ClassResolver classResolver) {
        return Collections.emptyList();
    }

    @Override
    public List<Statement> env(String varName, ClassResolver classResolver) {
        /*
        importCustomizer = new org.codehaus.groovy.control.customizers.ImportCustomizer()
        importCustomizer.addImport("ArrayList", "java.util.ArrayList")
        ...
        configuration = new org.codehaus.groovy.control.CompilerConfiguration()
        configuration.addCompilationCustomizers(importCustomizer)

        env = new groovy.lang.GroovyShell(configuration)
        */
        Var importCustomizerVar = new Var(genSym.generate("importCustomizer"));
        Stream<Statement> importCustomizer = Stream.of(
                new VarInitStatement(
                        IMPORT_CUSTOMIZER.toType(),
                        importCustomizerVar.getName(),
                        new NewExpr(
                                "org.codehaus.groovy.control.customizers.ImportCustomizer",
                                Collections.emptyList(),
                                Collections.emptyList()),
                        Span.dummySpan()));
        Stream<Statement> importClasses = classResolver.flatMap(
                (shortName, longName) -> {
                    Var shortNameVar = new Var(genSym.generate(SUtils.toIdent(shortName)));
                    Var longNameVar = new Var(genSym.generate(SUtils.toIdent(longName)));
                    Var __ = new Var(genSym.generate("__"));
                    return Stream.of(
                            new VarInitStatement(
                                    Type.STRING,
                                    shortNameVar.getName(),
                                    new StrLitExpr(shortName),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    Type.STRING,
                                    longNameVar.getName(),
                                    new StrLitExpr(longName),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    IMPORT_CUSTOMIZER.toType(),
                                    __.getName(),
                                    new InvokeExpr(
                                            IMPORT_CUSTOMIZER,
                                            importCustomizerVar,
                                            "addImport",
                                            Arrays.asList(Type.STRING, Type.STRING),
                                            Arrays.asList(shortNameVar, longNameVar),
                                            IMPORT_CUSTOMIZER.toType()),
                                    Span.dummySpan()));
                });

        Var configurationVar = new Var(genSym.generate("configuration"));
        Var customizersVar = new Var(genSym.generate("customizers"));
        Var __ = new Var(genSym.generate("__"));
        Stream<Statement> configuration = Stream.of(
                new VarInitStatement(
                        COMPILER_CONFIGURATION.toType(),
                        configurationVar.getName(),
                        new NewExpr(
                                "org.codehaus.groovy.control.CompilerConfiguration",
                                Collections.emptyList(),
                                Collections.emptyList()),
                        Span.dummySpan()),
                new VarInitStatement(
                        COMPILATION_CUSTOMIZER.toArray(),
                        customizersVar.getName(),
                        new ArrayExpr(
                                COMPILATION_CUSTOMIZER.toArray(),
                                Arrays.asList(importCustomizerVar)),
                        Span.dummySpan()),
                new VarInitStatement(
                        COMPILER_CONFIGURATION.toType(),
                        __.getName(),
                        new InvokeExpr(
                                COMPILER_CONFIGURATION,
                                configurationVar,
                                "addCompilationCustomizers",
                                Arrays.asList(COMPILATION_CUSTOMIZER.toArray()),
                                Arrays.asList(customizersVar),
                                COMPILER_CONFIGURATION.toType()),
                        Span.dummySpan()));

        Stream<Statement> groovyShell = Stream.of(
                new VarInitStatement(
                        GROOVY_SHELL.toType(),
                        varName,
                        new NewExpr(
                                "groovy.lang.GroovyShell",
                                Arrays.asList(COMPILER_CONFIGURATION.toType()),
                                Arrays.asList(configurationVar)),
                        Span.dummySpan()));

        return Stream.concat(importCustomizer,
                Stream.concat(importClasses,
                        Stream.concat(configuration, groovyShell)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Statement> bind(String envVarName, String name, Var rhs) {
        /*
        env.setVariable(name, rhs);
        */
        Var nameVar = new Var(genSym.generate(name));
        return Arrays.asList(
                new VarInitStatement(
                        Type.STRING,
                        nameVar.getName(),
                        new StrLitExpr(name),
                        Span.dummySpan()),
                new InvokeVoidStatement(
                        GROOVY_SHELL,
                        new Var(envVarName),
                        "setVariable",
                        Arrays.asList(Type.STRING, Type.OBJECT),
                        Arrays.asList(nameVar, rhs),
                        Span.dummySpan()));
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(String causeVarName) {
        return Optional.empty();
    }

    @Override
    public List<Statement> eval(
            String varName,
            QuotedExpr quotedExpr,
            Class<?> expectedType,
            String envVarName) {
        Var exprVar = new Var(genSym.generate("expression"));
        Span span = quotedExpr.getSpan();
        return Arrays.asList(
                new VarInitStatement(Type.STRING, exprVar.getName(),
                        new StrLitExpr(quotedExpr.getText()), Span.dummySpan()),
                new VarInitStatement(Type.fromClass(expectedType), varName,
                        new InvokeExpr(
                                GROOVY_SHELL,
                                new Var(envVarName),
                                "evaluate",
                                Arrays.asList(Type.STRING),
                                Arrays.asList(exprVar),
                                Type.OBJECT),
                        span));
    }
}
