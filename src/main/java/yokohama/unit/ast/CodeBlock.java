package yokohama.unit.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class CodeBlock implements Definition {
    Heading heading;
    String lang;
    List<String> lines;    
    Span span;

    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitCodeBlock(this);
    }
}
