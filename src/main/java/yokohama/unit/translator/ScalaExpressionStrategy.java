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
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.Statement;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<CatchClause> catchAndAssignCause(Sym causeVar) {
        return Optional.empty();
    }

    @Override
    public List<Statement> eval(Sym var, QuotedExpr quotedExpr, Class<?> expectedType, Sym envVar) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Statement> dumpEnv(Sym var, Sym envVar) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
