package yokohama.unit.translator;

import java.util.Arrays;
import java.util.List;
import yokohama.unit.ast_junit.InvokeVoidStatement;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.StrLitExpr;
import yokohama.unit.ast_junit.Var;
import yokohama.unit.ast_junit.VarDeclStatement;
import yokohama.unit.util.GenSym;

public class OgnlExpressionStrategy implements ExpressionStrategy {
    @Override
    public List<Statement> env(String varName) {
        return Arrays.asList(
                new VarDeclStatement(
                        varName,
                        new NewExpr("ognl.OgnlContext")));
    }

    @Override
    public List<Statement> bind(String envVarName, String name, Var rhs, GenSym genSym) {
        Var nameVar = new Var(genSym.generate(name));
        return Arrays.asList(
                new VarDeclStatement(nameVar.getName(), new StrLitExpr(name)),
                new InvokeVoidStatement(
                        new Var(envVarName),
                        "put",
                        Arrays.asList(nameVar, rhs)));
    }
}
