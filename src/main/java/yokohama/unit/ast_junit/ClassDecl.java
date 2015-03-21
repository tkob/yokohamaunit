package yokohama.unit.ast_junit;

import java.util.List;
import lombok.Value;
import lombok.experimental.NonFinal;
import yokohama.unit.util.SBuilder;

@Value
@NonFinal
public class ClassDecl {
    private final String name;
    private final List<TestMethod> testMethods;

    public void toString(
            SBuilder sb,
            ExpressionStrategy expressionStrategy,
            MockStrategy mockStrategy
    ) {
        sb.appendln("public class ", name, " {");
        sb.shift();
        if (testMethods.size() > 0) {
            expressionStrategy.auxMethods(sb);
            mockStrategy.auxMethods(sb);
        }
        for (TestMethod testMethod : testMethods) {
            testMethod.toString(sb, expressionStrategy, mockStrategy);
        }
        sb.unshift();
        sb.appendln("}");
    }

}
