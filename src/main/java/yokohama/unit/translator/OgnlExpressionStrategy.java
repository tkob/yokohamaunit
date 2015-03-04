package yokohama.unit.translator;

import java.util.Arrays;
import java.util.List;
import yokohama.unit.ast_junit.NewExpr;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.ast_junit.VarDeclStatement;

public class OgnlExpressionStrategy implements ExpressionStrategy {
    @Override
    public List<Statement> env(String varName) {
        return Arrays.asList(
                new VarDeclStatement(
                        "env",
                        new NewExpr("ognl.OgnlContext")));
    }
}
