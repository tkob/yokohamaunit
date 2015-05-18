package yokohama.unit.translator;

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
    public List<Statement> env(String varName) {
        /*
        javax.el.ELProcessor env = new javax.el.ELProcessor();
        env.getELManager().importClass("java.util.ArrayList")
        ...
        */
        Sym managerVar = new Sym(genSym.generate("manager"));
        Stream<Statement> newElp = Stream.of(new VarInitStatement(
                        EL_PROCESSOR.toType(),
                        varName,
                        new NewExpr(
                                "javax.el.ELProcessor",
                                Arrays.asList(),
                                Arrays.asList()),
                        Span.dummySpan()),
                new VarInitStatement(
                        EL_MANAGER.toType(),
                        managerVar.getName(),
                        new InvokeExpr(
                                EL_PROCESSOR,
                                new Sym(varName),
                                "getELManager",
                                Arrays.asList(),
                                Arrays.asList(),
                                EL_MANAGER.toType()),
                        Span.dummySpan()));
        Stream<Statement> importClasses = classResolver.<String>map((s, l) -> l)
                .flatMap(longName -> {
                    Sym longNameVar = new Sym(genSym.generate(SUtils.toIdent(longName)));
                    return Stream.of(
                            new VarInitStatement(
                                    Type.STRING,
                                    longNameVar.getName(),
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
    public List<Statement> bind(String envVarName, Ident ident, Sym rhs) {
        /*
        env.defineBean(name, rhs);
        */
        Sym nameVar = new Sym(genSym.generate(ident.getName()));
        return Arrays.asList(new VarInitStatement(
                        Type.STRING,
                        nameVar.getName(),
                        new StrLitExpr(ident.getName()),
                        ident.getSpan()),
                new InvokeVoidStatement(
                        EL_PROCESSOR,
                        new Sym(envVarName),
                        "defineBean",
                        Arrays.asList(Type.STRING, Type.OBJECT),
                        Arrays.asList(nameVar, rhs),
                        Span.dummySpan()));
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(String causeVarName) {
        Sym caughtVar = new Sym(genSym.generate("ex"));
        Sym reasonVar = new Sym(genSym.generate("reason"));
        Sym nullValueVar = new Sym(genSym.generate("nullValue"));
        Sym condVar = new Sym(genSym.generate("cond"));
        return Optional.of(new CatchClause(
                EL_EXCEPTION,
                caughtVar,
                Arrays.asList(
                        new VarInitStatement(
                                Type.THROWABLE,
                                reasonVar.getName(),
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
                                nullValueVar.getName(),
                                new NullExpr(),
                                Span.dummySpan()),
                        new VarInitStatement(
                                Type.BOOLEAN,
                                condVar.getName(),
                                new EqualOpExpr(reasonVar, nullValueVar),
                                Span.dummySpan()),
                        new IfStatement(
                                condVar,
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVarName,
                                                new VarExpr(caughtVar.getName()),
                                                Span.dummySpan())),
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVarName,
                                                new VarExpr(reasonVar.getName()),
                                                Span.dummySpan()))))));
    }

    @Override
    public List<Statement> eval(
            String varName,
            QuotedExpr quotedExpr,
            Class<?> expectedType,
            String envVarName) {
        Sym exprVar = new Sym(genSym.generate("expression"));
        Sym expectedTypeVar = new Sym(genSym.generate("expectedType"));
        Span span = quotedExpr.getSpan();
        return Arrays.asList(new VarInitStatement(Type.STRING, exprVar.getName(),
                        new StrLitExpr(quotedExpr.getText()), span),
                new VarInitStatement(
                        Type.CLASS, expectedTypeVar.getName(),
                        new ClassLitExpr(Type.fromClass(expectedType).box()),
                        Span.dummySpan()),
                new VarInitStatement(Type.fromClass(expectedType), varName,
                        new InvokeExpr(
                                EL_PROCESSOR,
                                new Sym(envVarName),
                                "getValue",
                                Arrays.asList(Type.STRING, Type.CLASS),
                                Arrays.asList(exprVar, expectedTypeVar),
                                Type.OBJECT),
                        Span.dummySpan()));
    }
}
