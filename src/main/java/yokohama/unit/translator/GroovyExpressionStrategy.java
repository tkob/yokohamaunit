package yokohama.unit.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import yokohama.unit.ast.Ident;
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
import yokohama.unit.util.Sym;
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
    private final ClassResolver classResolver;

    static final String COMPILATION_CUSTOMIZER =
            "org.codehaus.groovy.control.customizers.CompilationCustomizer";
    static final String IMPORT_CUSTOMIZER =
            "org.codehaus.groovy.control.customizers.ImportCustomizer";
    static final String COMPILER_CONFIGURATION =
            "org.codehaus.groovy.control.CompilerConfiguration";
    static final String GROOVY_SHELL = "groovy.lang.GroovyShell";

    @SneakyThrows(ClassNotFoundException.class)
    ClassType classTypeOf(String name) {
        return new ClassType(classResolver.lookup(name));
    }
    Type typeOf(String name) {
        return classTypeOf(name).toType();
    }

    @Override
    public Collection<ClassDecl> auxClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<Statement> env(Sym var) {
        /*
        importCustomizer = new org.codehaus.groovy.control.customizers.ImportCustomizer()
        importCustomizer.addImport("ArrayList", "java.util.ArrayList")
        ...
        configuration = new org.codehaus.groovy.control.CompilerConfiguration()
        configuration.addCompilationCustomizers(importCustomizer)

        env = new groovy.lang.GroovyShell(configuration)
        */
        Sym importCustomizerVar = genSym.generate("importCustomizer");
        Stream<Statement> importCustomizer = Stream.of(
                new VarInitStatement(
                        typeOf(IMPORT_CUSTOMIZER),
                        importCustomizerVar,
                        new NewExpr(
                                "org.codehaus.groovy.control.customizers.ImportCustomizer",
                                Collections.emptyList(),
                                Collections.emptyList()),
                        Span.dummySpan()));
        Stream<Statement> importClasses = classResolver.flatMap((shortName, longName) -> {
                    Sym shortNameVar = genSym.generate(SUtils.toIdent(shortName));
                    Sym longNameVar = genSym.generate(SUtils.toIdent(longName));
                    Sym __ = genSym.generate("__");
                    return Stream.of(
                            new VarInitStatement(
                                    Type.STRING,
                                    shortNameVar,
                                    new StrLitExpr(shortName),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    Type.STRING,
                                    longNameVar,
                                    new StrLitExpr(longName),
                                    Span.dummySpan()),
                            new VarInitStatement(
                                    typeOf(IMPORT_CUSTOMIZER),
                                    __,
                                    new InvokeExpr(
                                            classTypeOf(IMPORT_CUSTOMIZER),
                                            importCustomizerVar,
                                            "addImport",
                                            Arrays.asList(Type.STRING, Type.STRING),
                                            Arrays.asList(shortNameVar, longNameVar),
                                            typeOf(IMPORT_CUSTOMIZER)),
                                    Span.dummySpan()));
                });

        Sym configurationVar = genSym.generate("configuration");
        Sym customizersVar = genSym.generate("customizers");
        Sym __ = genSym.generate("__");
        Stream<Statement> configuration = Stream.of(
                new VarInitStatement(
                        typeOf(COMPILER_CONFIGURATION),
                        configurationVar,
                        new NewExpr(
                                "org.codehaus.groovy.control.CompilerConfiguration",
                                Collections.emptyList(),
                                Collections.emptyList()),
                        Span.dummySpan()),
                new VarInitStatement(
                        typeOf(COMPILATION_CUSTOMIZER).toArray(),
                        customizersVar,
                        new ArrayExpr(
                                typeOf(COMPILATION_CUSTOMIZER).toArray(),
                                Arrays.asList(importCustomizerVar)),
                        Span.dummySpan()),
                new VarInitStatement(
                        typeOf(COMPILER_CONFIGURATION),
                        __,
                        new InvokeExpr(
                                classTypeOf(COMPILER_CONFIGURATION),
                                configurationVar,
                                "addCompilationCustomizers",
                                Arrays.asList(typeOf(COMPILATION_CUSTOMIZER).toArray()),
                                Arrays.asList(customizersVar),
                                typeOf(COMPILER_CONFIGURATION)),
                        Span.dummySpan()));

        Stream<Statement> groovyShell = Stream.of(
                new VarInitStatement(
                        typeOf(GROOVY_SHELL),
                        var,
                        new NewExpr(
                                "groovy.lang.GroovyShell",
                                Arrays.asList(typeOf(COMPILER_CONFIGURATION)),
                                Arrays.asList(configurationVar)),
                        Span.dummySpan()));

        return Stream.concat(importCustomizer,
                Stream.concat(importClasses,
                        Stream.concat(configuration, groovyShell)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Statement> bind(Sym envVar, Ident ident, Sym rhs) {
        /*
        env.setVariable(name, rhs);
        */
        Sym nameVar = genSym.generate(ident.getName());
        return Arrays.asList(new VarInitStatement(
                        Type.STRING,
                        nameVar,
                        new StrLitExpr(ident.getName()),
                        ident.getSpan()),
                new InvokeVoidStatement(
                        classTypeOf(GROOVY_SHELL),
                        envVar,
                        "setVariable",
                        Arrays.asList(Type.STRING, Type.OBJECT),
                        Arrays.asList(nameVar, rhs),
                        Span.dummySpan()));
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(Sym causeVar) {
        return Optional.empty();
    }

    @Override
    public List<Statement> eval(
            Sym var,
            QuotedExpr quotedExpr,
            Class<?> expectedType,
            Sym envVar) {
        Sym exprVar = genSym.generate("expression");
        Span span = quotedExpr.getSpan();
        return Arrays.asList(new VarInitStatement(Type.STRING, exprVar,
                        new StrLitExpr(quotedExpr.getText()), span),
                new VarInitStatement(Type.fromClass(expectedType), var,
                        new InvokeExpr(
                                classTypeOf(GROOVY_SHELL),
                                envVar,
                                "evaluate",
                                Arrays.asList(Type.STRING),
                                Arrays.asList(exprVar),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    @Override
    public List<Statement> dumpEnv(Sym var, Sym envVar) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
