package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Table implements Definition {
    private String name;
    private List<String> header;
    private List<Row> rows;
    
    @Override
    public <T> T accept(DefinitionVisitor<T> visitor) {
        return visitor.visitTable(this);
    }
}
