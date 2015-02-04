package yokohama.unit.ast;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(exclude={"span"})
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class InstanceSuchThatMatcher implements Matcher {
    private String varName;
    private ClassType clazz;
    private List<Proposition> propositions;
    private Span span;

    @Override
    public <T> T accept(MatcherVisitor<T> visitor) {
        return visitor.visitInstanceSuchThat(this);
    }

    @Override
    public String getDesctiption() {
        return "an instance of " + clazz.getName() + " s.t. ...";
    }
}
