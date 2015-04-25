package yokohama.unit.ast;

import yokohama.unit.position.Span;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"}, callSuper=false)
public class TableRef extends Fixture {
    private List<Ident> idents;
    private TableType type;
    private String name;
    private Span span;

    @Override
    public <T> T accept(FixtureVisitor<T> visitor) {
        return visitor.visitTableRef(this);
    }
    
}
