package yokohama.unit.ast;

import lombok.Value;

@Value
public class Proposition {
    private QuotedExpr subject;
    private Copula copula;
    private QuotedExpr complement;
}
