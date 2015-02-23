package yokohama.unit.ast;
    
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class LetBinding {
    private String name;
    private Expr value;
    private Span span;
}
