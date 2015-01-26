package yokohama.unit.ast_junit;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Value;
import static yokohama.unit.util.SetUtils.setOf;

@Value
@EqualsAndHashCode(callSuper = false)
public class ConjunctionMatcherExpr extends MatcherExpr {
    private final List<Var> matchers;

    @Override
    public String getExpr() {
        return "allOf(" +
                matchers.stream().map(Var::getName).collect(Collectors.joining(", ")) +
                ")";
    }

    @Override
    public Set<ImportedName> importedNames() {
        return setOf(new ImportStatic("org.hamcrest.CoreMatchers.allOf"));
    }
    
}
