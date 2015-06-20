package yokohama.unit.translator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import yokohama.unit.ast.Ident;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast_junit.BooleanLitExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassDecl;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.FieldStaticExpr;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.position.Span;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;
import yokohama.unit.util.Sym;

@RequiredArgsConstructor
public class ScalaExpressionStrategy implements ExpressionStrategy {
    final String name;
    final String packageName;
    final GenSym genSym;
    final ClassResolver classResolver;

    static final String IMAIN = "scala.tools.nsc.interpreter.IMain";
    static final String SETTINGS = "scala.tools.nsc.Settings";
    static final String BOOLEAN_SETTING =
            "scala.tools.nsc.settings.MutableSettings$BooleanSetting";
    static final String NIL$ = "scala.collection.immutable.Nil$";
    static final String LIST = "scala.collection.immutable.List";
    static final String RESULT = "scala.tools.nsc.interpreter.Results$Result";
    static final String OPTION = "scala.Option";

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
        Sym settings = genSym.generate("settings");
        Sym usejavacp = genSym.generate("usejavacp");
        Sym true_ = genSym.generate("true_");
        return Arrays.asList(
                new VarInitStatement(
                        typeOf(SETTINGS),
                        settings,
                        new NewExpr(
                                SETTINGS,
                                Arrays.asList(),
                                Arrays.asList()),
                        Span.dummySpan()),
                new VarInitStatement(
                        typeOf(SETTINGS),
                        usejavacp,
                        new InvokeExpr(
                                classTypeOf(SETTINGS),
                                settings,
                                "usejavacp",
                                Arrays.asList(),
                                Arrays.asList(),
                                typeOf(BOOLEAN_SETTING)),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.BOOLEAN,
                        true_,
                        new BooleanLitExpr(true),
                        Span.dummySpan()),
                new InvokeVoidStatement(
                        classTypeOf(BOOLEAN_SETTING),
                        usejavacp,
                        "v_$eq",
                        Arrays.asList(Type.BOOLEAN),
                        Arrays.asList(true_),
                        Span.dummySpan()),
                new VarInitStatement(
                        typeOf(IMAIN),
                        var,
                        new NewExpr(
                                IMAIN,
                                Arrays.asList(typeOf(SETTINGS)),
                                Arrays.asList(settings)),
                        Span.dummySpan()));
    }

    @Override
    public List<Statement> bind(Sym envVar, Ident ident, Sym rhs) {
        Sym nameVar = genSym.generate(ident.getName());
        Sym boundTypeVar = genSym.generate("boundType");
        Sym modifiersVar = genSym.generate("modifiers");
        return Arrays.asList(
                new VarInitStatement(
                        Type.STRING,
                        nameVar,
                        new StrLitExpr(ident.getName()),
                        ident.getSpan()),
                new VarInitStatement(
                        Type.STRING,
                        boundTypeVar,
                        new StrLitExpr("java.lang.Object"),
                        Span.dummySpan()),
                new VarInitStatement(
                        typeOf(LIST),
                        modifiersVar,
                        new FieldStaticExpr(
                                classTypeOf(NIL$),
                                typeOf(LIST),
                                "MODULE$"),
                        Span.dummySpan()),
                new VarInitStatement(
                        typeOf(RESULT),
                        genSym.generate("__"),
                        new InvokeExpr(
                                classTypeOf(IMAIN),
                                envVar,
                                "bind",
                                Arrays.asList(
                                        Type.STRING,
                                        Type.STRING,
                                        Type.OBJECT,
                                        typeOf(LIST)),
                                Arrays.asList(
                                        nameVar,
                                        boundTypeVar,
                                        rhs,
                                        modifiersVar),
                                typeOf(RESULT)),
                        Span.dummySpan()));
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(Sym causeVar) {
        return Optional.empty();
    }

    @Override
    public List<Statement> eval(Sym var, QuotedExpr quotedExpr, Class<?> expectedType, Sym envVar) {
        Span span = quotedExpr.getSpan();
        Sym exprVar = genSym.generate("expression");
        Sym resultVar = genSym.generate("result");
        Sym mostRecentVar = genSym.generate("mostRecent");
        Sym valueOfTermVar = genSym.generate("valueOfTerm");
        return Arrays.asList(
                new VarInitStatement(
                        Type.STRING,
                        exprVar,
                        new StrLitExpr(quotedExpr.getText()),
                        span),
                new VarInitStatement(
                        typeOf(RESULT),
                        resultVar,
                        new InvokeExpr(
                                classTypeOf(IMAIN),
                                envVar,
                                "interpret",
                                Arrays.asList(Type.STRING),
                                Arrays.asList(exprVar),
                                typeOf(RESULT)),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.STRING,
                        mostRecentVar,
                        new InvokeExpr(
                                classTypeOf(IMAIN),
                                envVar,
                                "mostRecnetVar",
                                Arrays.asList(),
                                Arrays.asList(),
                                Type.STRING),
                        Span.dummySpan()),
                new VarInitStatement(
                        typeOf(OPTION),
                        valueOfTermVar,
                        new InvokeExpr(
                                classTypeOf(IMAIN),
                                envVar,
                                "valueOfTerm",
                                Arrays.asList(Type.STRING),
                                Arrays.asList(mostRecentVar),
                                typeOf(OPTION)),
                        Span.dummySpan()),
                new VarInitStatement(
                        Type.fromClass(expectedType),
                        var,
                        new InvokeExpr(
                                classTypeOf(OPTION),
                                valueOfTermVar,
                                "get",
                                Arrays.asList(),
                                Arrays.asList(),
                                Type.OBJECT),
                        Span.dummySpan()));
    }

    @Override
    public List<Statement> dumpEnv(Sym var, Sym envVar) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
