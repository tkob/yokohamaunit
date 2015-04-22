package yokohama.unit.translator;

import java.util.stream.Stream;
import yokohama.unit.ast.StubExpr;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.util.ClassResolver;

public interface MockStrategy {
    /**
     * Create a stub.
     * 
     * @param varName            a variable name to be bound to the stub
     * @param stubExpr           a stub expression
     * @param expressionStrategy
     * @param envVarName         a variable name bound to the environment
     * @param classResolver
     * @return statements that bind the variable name to the stub
     */
    Stream<Statement> stub(
            String varName,
            StubExpr stubExpr,
            ExpressionStrategy expressionStrategy,
            String envVarName,
            ClassResolver classResolver);
}
