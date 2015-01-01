package yokohama.unit.ast;

import lombok.Value;

@Value
public class Type {
    private NonArrayType nonArrayType;
    private int dims;
}
