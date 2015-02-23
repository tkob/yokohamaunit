package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Proposition implements Describable {
    private QuotedExpr subject;
    private Predicate predicate;
    private Span span;

    @Override
    public String getDescription() {
        return "`" + subject.getText() + "` " + predicate.getDescription();
    }
}
