package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import yokohama.unit.ast.QuotedExpr;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.EqualOpExpr;
import yokohama.unit.ast_junit.IfStatement;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeStaticExpr;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.NullExpr;
import yokohama.unit.ast_junit.Span;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Type;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.util.GenSym;

public class OgnlExpressionStrategy implements ExpressionStrategy {
    static final Type OGNL_CONTEXT = new Type(new ClassType("ognl.OgnlContext", Span.dummySpan()), 0);
    static final ClassType OGNL_EXCEPTION = new ClassType("ognl.OgnlException", Span.dummySpan());

    @Override
    public List<Statement> env(String varName) {
        return Arrays.asList(new VarInitStatement(
                OGNL_CONTEXT,
                varName,
                new NewExpr("ognl.OgnlContext"),
                Span.dummySpan()));
    }

    @Override
    public List<Statement> bind(String envVarName, String name, Var rhs, GenSym genSym) {
        Var nameVar = new Var(genSym.generate(name));
        return Arrays.asList(
                new VarInitStatement(
                        Type.STRING,
                        nameVar.getName(),
                        new StrLitExpr(name),
                        Span.dummySpan()),
                new InvokeVoidStatement(
                        new Var(envVarName),
                        "put",
                        Arrays.asList(nameVar, rhs)));
    }

    @Override
    public CatchClause catchAndAssignCause(String caughtVarName, String causeVarName, GenSym genSym) {
        Var caughtVar = new Var(caughtVarName);
        Var reasonVar = new Var(genSym.generate("reason"));
        Var nullValueVar = new Var(genSym.generate("nullValue"));
        Var condVar = new Var(genSym.generate("cond"));
        return new CatchClause(
                OGNL_EXCEPTION,
                caughtVar,
                Arrays.asList(
                        new VarInitStatement(
                                Type.THROWABLE,
                                reasonVar.getName(),
                                new InvokeExpr(caughtVar, "getReason", Arrays.asList()),
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
                                                new VarExpr(caughtVarName),
                                                Span.dummySpan())),
                                Arrays.asList(
                                        new VarInitStatement(
                                                Type.THROWABLE,
                                                causeVarName,
                                                new VarExpr(reasonVar.getName()),
                                                Span.dummySpan())))));
    }

    @Override
    public List<Statement> eval(
            String varName,
            String envVarName,
            QuotedExpr quotedExpr,
            GenSym genSym,
            Optional<Path> docyPath,
            String className,
            String packageName) {
        Var exprVar = new Var(genSym.generate("expression"));
        Span span = new Span(
                docyPath,
                quotedExpr.getSpan().getStart(),
                quotedExpr.getSpan().getEnd());
        return Arrays.asList(
                new VarInitStatement(Type.STRING, exprVar.getName(),
                        new StrLitExpr(quotedExpr.getText()), Span.dummySpan()),
                new VarInitStatement(Type.OBJECT, varName,
                        new InvokeStaticExpr(
                                new ClassType("ognl.Ognl", Span.dummySpan()),
                                Arrays.asList(),
                                "getValue",
                                Arrays.asList(
                                        exprVar,
                                        new Var(envVarName))),
                        span));
    }
}
