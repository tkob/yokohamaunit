package yokohama.unit.ast;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode(exclude={"span"})
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter
public class Table implements Definition {
    private String name;
    private List<TableHeaderCell> header;
    private List<Row> rows;
    private Span span;
    
    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitTable(this);
    }
}
