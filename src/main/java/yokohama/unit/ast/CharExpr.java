package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringEscapeUtils;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class CharExpr implements Expr {
    private final String literal;
    private final Span span;

    public char getValue() {
        return StringEscapeUtils.unescapeJava(literal).charAt(0);
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitCharExpr(this);
    }
}
