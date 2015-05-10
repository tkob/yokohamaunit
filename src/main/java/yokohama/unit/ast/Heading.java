package yokohama.unit.ast;

import lombok.EqualsAndHashCode;
import lombok.Value;
import yokohama.unit.position.Span;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Heading implements Definition {
    String line;    
    Span span;

    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitHeading(this);
    }
}
