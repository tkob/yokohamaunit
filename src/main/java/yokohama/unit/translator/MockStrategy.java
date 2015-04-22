package yokohama.unit.translator;

import java.util.List;
import java.util.stream.Stream;
import yokohama.unit.position.Span;
import yokohama.unit.ast.StubBehavior;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.util.ClassResolver;

public interface MockStrategy {
    Stream<Statement> stub(
            String varName,
            String classToStubName,
            Span classToStubSpan,
            List<StubBehavior> behavior,
            ExpressionStrategy expressionStrategy,
            String envVarName,
            ClassResolver classResolver);
}
