package yokohama.unit.ast;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(exclude={"span"}, callSuper=false)
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
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
