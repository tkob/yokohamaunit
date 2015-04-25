package yokohama.unit.ast;

import yokohama.unit.position.Span;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(exclude={"span"})
public class Type {
    private NonArrayType nonArrayType;
    private int dims;
    private Span span;

    public Type toArray() {
        return new Type(nonArrayType, dims + 1, span);
    }
}
