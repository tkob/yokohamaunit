package yokohama.unit.ast;

import lombok.Value;

@Value
public class TableRef extends Fixture {
    private TableType type;
    private String name;

    @Override
    public <T> T accept(FixtureVisitor<T> visitor) {
        return visitor.visitTableRef(this);
    }
    
}
