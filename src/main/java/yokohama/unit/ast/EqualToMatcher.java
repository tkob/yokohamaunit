package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class EqualToMatcher implements Matcher {
    private QuotedExpr expr;
    private Span span;

    @Override
    public <T> T accept(MatcherVisitor<T> visitor) {
        return visitor.visitEqualTo(this);
    }

    @Override
    public String getDescription() {
        return "`" + expr.getText() + "`";
    }
    
}
