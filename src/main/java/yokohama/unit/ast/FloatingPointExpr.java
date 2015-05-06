package yokohama.unit.ast;

import java.util.function.DoubleFunction;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class FloatingPointExpr implements Expr {
    private final boolean positive;
    private final String literal;
    private final Span span;

    public <T> T match(Function<Float, T> floatf, DoubleFunction<T> doublef) {
        boolean isFloat = StringUtils.endsWithIgnoreCase(literal, "f");
        String signedLiteral = (positive ? "" : "-") + literal;
        if (isFloat) {
            return floatf.apply(Float.parseFloat(signedLiteral));
        } else {
            return doublef.apply(Double.parseDouble(signedLiteral));
        }
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitFloatingPointExpr(this);
    }
}
