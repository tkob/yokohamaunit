package yokohama.unit.ast_junit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
@EqualsAndHashCode(callSuper = false)
public class ConjunctionMatcherExpr extends MatcherExpr {
    private final List<Var> matchers;

    @Override
    public void getExpr(SBuilder sb, String varName, ExpressionStrategy expressionStrategy, MockStrategy mockStrategy) {
        sb.appendln(varName, " = org.hamcrest.CoreMatchers.allOf(",
                matchers.stream().map(Var::getName).collect(Collectors.joining(", ")),
                ");");
    }

}
