package yokohama.unit.ast;
    
import lombok.Value;

@Value
public class LetBinding {
    private String name;
    private Expr value;
}
