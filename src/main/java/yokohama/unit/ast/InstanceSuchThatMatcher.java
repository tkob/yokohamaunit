package yokohama.unit.ast;

import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class InstanceSuchThatMatcher implements Matcher {
    private Ident var;
    private ClassType clazz;
    private List<Proposition> propositions;
    private Span span;

    @Override
    public <T> T accept(MatcherVisitor<T> visitor) {
        return visitor.visitInstanceSuchThat(this);
    }

    @Override
    public String getDescription() {
        return "an instance " + var.getName() + " of " + clazz.getName() + " s.t. " +
                propositions.stream().map(Proposition::getDescription).collect(Collectors.joining(" and "));
    }
}
