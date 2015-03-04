package yokohama.unit.translator;

import java.util.List;
import yokohama.unit.ast_junit.Statement;

public interface ExpressionStrategy {
    List<Statement> env(String varName); // introduce new environment
}
