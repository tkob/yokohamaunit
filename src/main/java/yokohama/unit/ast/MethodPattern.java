package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class MethodPattern {
    private String name;
    private List<Type> argumentTypes;
    private boolean varArg;
}
