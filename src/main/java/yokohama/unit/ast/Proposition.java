package yokohama.unit.ast;

import lombok.Value;

@Value
public class Proposition {
    private Expr subject;
    private Copula copula;
    private Expr complement;
}
