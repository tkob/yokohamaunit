package yokohama.unit.ast;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class IntegerExpr implements Expr {
    private final boolean positive;
    private final String literal;
    private final Span span;

    public <T> T match(IntFunction<T> intf, LongFunction<T> longf) {
        boolean isLong = StringUtils.endsWithIgnoreCase(literal, "L");
        String digits = Function.<String>identity()
                .andThen(s -> StringUtils.removeEndIgnoreCase(s, "L"))
                .andThen(s -> StringUtils.removeStartIgnoreCase(s, "0x"))
                .andThen(s -> StringUtils.removeStartIgnoreCase(s, "0b"))
                .andThen(s -> StringUtils.remove(s, "_"))
                .apply(literal);
        int radix = StringUtils.startsWithIgnoreCase(literal, "0x") ? 16
                :   StringUtils.startsWithIgnoreCase(literal, "0b") ?  2
                :   StringUtils.startsWithIgnoreCase(literal, "0")  ?  8
                :                                                     10;
        if (isLong) {
            return positive
                    ? longf.apply(Long.parseUnsignedLong(digits, radix))
                    : longf.apply(Long.parseLong("-" + digits, radix));
        } else {
            return positive
                    ? intf.apply(Integer.parseUnsignedInt(digits, radix))
                    : intf.apply(Integer.parseInt("-" + digits, radix));
        }
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitIntegerExpr(this);
    }
}
