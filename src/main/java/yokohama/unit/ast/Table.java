package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Table implements Definition {
    private String name;
    private List<Ident> header;
    private List<Row> rows;
    private Span span;
    
    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitTable(this);
    }
}
