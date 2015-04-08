package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Optional;
import lombok.Value;
import lombok.experimental.NonFinal;
import yokohama.unit.util.SBuilder;

@Value
@NonFinal
public class ClassDecl {
    private final String name;
    private final Optional<ClassType> extended;
    private final List<ClassType> implemented;
    private final List<TestMethod> testMethods;

    public void toString(SBuilder sb) {
        sb.appendln("public class ", name, " {");
        sb.shift();
        for (TestMethod testMethod : testMethods) {
            testMethod.toString(sb);
        }
        sb.unshift();
        sb.appendln("}");
    }

}
