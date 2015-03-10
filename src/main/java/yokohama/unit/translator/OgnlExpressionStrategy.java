package yokohama.unit.translator;

import java.util.Arrays;
import java.util.List;
import yokohama.unit.ast_junit.CatchClause;
import yokohama.unit.ast_junit.ClassType;
import yokohama.unit.ast_junit.InvokeExpr;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.Span;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarExpr;
import yokohama.unit.ast_junit.VarInitStatement;
import yokohama.unit.util.GenSym;

public class OgnlExpressionStrategy implements ExpressionStrategy {
    static final ClassType OGNL_CONTEXT = new ClassType("ognl.OgnlContext", Span.dummySpan());
    static final ClassType OGNL_EXCEPTION = new ClassType("ognl.OgnlException", Span.dummySpan());

    @Override
    public List<Statement> env(String varName) {
        return Arrays.asList(new VarInitStatement(
                OGNL_CONTEXT,
                varName,
                new NewExpr("ognl.OgnlContext")));
    }

    @Override
    public List<Statement> bind(String envVarName, String name, Var rhs, GenSym genSym) {
        Var nameVar = new Var(genSym.generate(name));
        return Arrays.asList(
                new VarInitStatement(
                        ClassType.STRING,
                        nameVar.getName(),
                        new StrLitExpr(name)),
                new InvokeVoidStatement(
                        new Var(envVarName),
                        "put",
                        Arrays.asList(nameVar, rhs)));
    }

    @Override
    public CatchClause catchAndAssignCause(String caughtVarName, String causeVarName, GenSym genSym) {
        Var caughtVar = new Var(caughtVarName);
        String cause = genSym.generate("cause");
        return new CatchClause(
                OGNL_EXCEPTION,
                caughtVar,
                Arrays.asList(
                        new VarInitStatement(
                                ClassType.THROWABLE,
                                cause,
                                new InvokeExpr(caughtVar, "getReason", Arrays.asList())),
                        new VarInitStatement(
                                ClassType.THROWABLE,
                                causeVarName,
                                new VarExpr(cause))));
    }
}
