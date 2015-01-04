package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class QuotedExpr implements Expr {
    private String text;
    private Span span;

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitQuotedExpr(this);
    }
}
