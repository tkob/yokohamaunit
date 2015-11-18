package yokohama.unit.translator.experimental;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.el.ELException;
import javax.el.ELManager;
import javax.el.ELProcessor;
import lombok.AllArgsConstructor;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassLitExpr;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.EqualOpExpr;
import yokohama.unit.ast_junit.IfStatement;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.NullExpr;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.util.Sym;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.position.Span;
import yokohama.unit.translator.ExpressionStrategy;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.SUtils;

@AllArgsConstructor
public class ElExpressionStrategy implements ExpressionStrategy {
    private final String name;
    private final String packageName;
    private final GenSym genSym;
    private final ClassResolver classResolver;

    static final ClassType EL_PROCESSOR = new ClassType(ELProcessor.class);
    static final ClassType EL_MANAGER = new ClassType(ELManager.class);
    static final ClassType EL_EXCEPTION = new ClassType(ELException.class);

    @Override
    public Collection<ClassDecl> auxClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<Statement> env(Sym var) {
        /*
        javax.el.ELProcessor env = new javax.el.ELProcessor();
        env.getELManager().importClass("java.util.ArrayList")
        ...
        */
        Sym managerVar = genSym.generate("manager");
        Stream<Statement> newElp = Stream.of(new VarInitStatement(
                        EL_PROCESSOR.toType(),
                        var,
                        new NewExpr(
                                "javax.el.ELProcessor",
                                Arrays.asList(),
                                Arrays.asList()),
                        Span.dummySpan()),
                new VarInitStatement(
                        EL_MANAGER.toType(),
                        managerVar,
                        new InvokeExpr(
                                EL_PROCESSOR,
                                var,
                                "getELManager",
                                Arrays.asList(),
                                Arrays.asList(),
                                EL_MANAGER.toType()),
                        Span.dummySpan()));
        Stream<Statement> importClasses = classResolver.<String>map((s, l) -> l)
                .flatMap(longName -> {
                    Sym longNameVar = genSym.generate(SUtils.toIdent(longName));
                    return Stream.of(
                            new VarInitStatement(
                                    Type.STRING,
                                    longNameVar,
                                    new StrLitExpr(longName),
                                    Span.dummySpan()),
                            new InvokeVoidStatement(
                                    EL_MANAGER,
                                    managerVar,
                                    "importClass",
                                    Arrays.asList(Type.STRING),
                                    Arrays.asList(longNameVar),
                                    Span.dummySpan()));
                });

        return Stream.concat(newElp, importClasses).collect(Collectors.toList());
    }

    @Override
    public List<Statement> bind(Sym envVar, Ident ident, Sym rhs) {
        /*
        env.defineBean(name, rhs);
        */
        Sym nameVar = genSym.generate(ident.getName());
        return Arrays.asList(new VarInitStatement(
                        Type.STRING,
                        nameVar,
                        new StrLitExpr(ident.getName()),
                        ident.getSpan()),
                new InvokeVoidStatement(
                        EL_PROCESSOR,
                        envVar,
                        "defineBean",
                        Arrays.asList(Type.STRING, Type.OBJECT),
                        Arrays.asList(nameVar, rhs),
                        Span.dummySpan()));
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(Sym causeVar) {
        Sym caughtVar = genSym.generate("ex");
        Sym reasonVar = genSym.generate("reason");
        Sym nullValueVar = genSym.generate("nullValue");
        Sym condVar = genSym.generate("cond");
        return Optional.of(new CatchClause(
                EL_EXCEPTION,
                caughtVar,
                Arrays.asList(
                        new VarInitStatement(
                                Type.THROWABLE,
                                reasonVar,
                                new InvokeExpr(
                                        EL_EXCEPTION,
                                        caughtVar,
                                        "getCause",
                                        Arrays.asList(),
                                        Arrays.asList(),
                                        Type.THROWABLE),
                                Span.dummySpan()),
                        new VarInitStatement(
                                Type.THROWABLE,
                                nullValueVar,
                                new NullExpr(),
                                Span.dummySpan()),
                        new VarInitStatement(
                                Type.BOOLEAN,
                                condVar,
                                new EqualOpExpr(reasonVar, nullValueVar),
                                Span.dummySpan()),
                        new IfStatement(
                                condVar,
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVar,
                                                new VarExpr(caughtVar),
                                                Span.dummySpan())),
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVar,
                                                new VarExpr(reasonVar),
                                                Span.dummySpan()))))));
    }

    @Override
    public List<Statement> eval(
            Sym var,
            QuotedExpr quotedExpr,
            Class<?> expectedType,
            Sym envVar) {
        Sym exprVar = genSym.generate("expression");
        Sym expectedTypeVar = genSym.generate("expectedType");
        Span span = quotedExpr.getSpan();
        return Arrays.asList(new VarInitStatement(Type.STRING, exprVar,
                        new StrLitExpr(quotedExpr.getText()), span),
                new VarInitStatement(
                        Type.CLASS, expectedTypeVar,
                        new ClassLitExpr(Type.fromClass(expectedType).box()),
                        Span.dummySpan()),
                new VarInitStatement(Type.fromClass(expectedType), var,
                        new InvokeExpr(
                                EL_PROCESSOR,
                                envVar,
                                "getValue",
                                Arrays.asList(Type.STRING, Type.CLASS),
                                Arrays.asList(exprVar, expectedTypeVar),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    @Override
    public List<Statement> dumpEnv(Sym var, Sym envVar) {
        // It seems that there is no way to dump environment in EL
        return Arrays.asList(
                new VarInitStatement(
                        Type.STRING,
                        var,
                        new StrLitExpr(""),
                        Span.dummySpan()));
    }
}
