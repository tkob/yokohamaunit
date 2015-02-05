package yokohama.unit.ast;

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
public class Proposition implements Describable {
    private QuotedExpr subject;
    private Predicate predicate;
    private Span span;

    @Override
    public String getDescription() {
        return "`" + subject.getText() + "` " + predicate.getDescription();
    }
}
