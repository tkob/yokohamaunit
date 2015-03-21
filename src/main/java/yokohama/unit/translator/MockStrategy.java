package yokohama.unit.translator;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import yokohama.unit.ast.ClassType;
import yokohama.unit.ast.StubBehavior;
import yokohama.unit.ast_junit.Statement;
import yokohama.unit.util.GenSym;

public interface MockStrategy {
    Stream<Statement> stub(
            String varName,
            ClassType classToStub,
            List<StubBehavior> behavior,
            ExpressionStrategy expressionStrategy,
            String envVarName,
            GenSym genSym,
            Optional<Path> docyPath,
            String className,
            String packageName);
}
