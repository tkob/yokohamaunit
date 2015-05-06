package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringEscapeUtils;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class StringExpr implements Expr {
    private final String literal;
    private final Span span;

    public String getValue() {
        return StringEscapeUtils.unescapeJava(literal);
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitStringExpr(this);
    }

    
}
