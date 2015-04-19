package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import yokohama.unit.ast.Span;
import yokohama.unit.ast.StubBehavior;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;

public interface MockStrategy {
    Stream<Statement> stub(
            String varName,
            String classToStubName,
            Span classToStubSpan,
            List<StubBehavior> behavior,
            ExpressionStrategy expressionStrategy,
            String envVarName,
            ClassResolver classResolver,
            GenSym genSym,
            String className,
            String packageName);
}
