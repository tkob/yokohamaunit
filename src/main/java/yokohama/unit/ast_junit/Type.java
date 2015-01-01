package yokohama.unit.ast_junit;

import lombok.Value;

@Value
public class Type {
    private NonArrayType nonArrayType;
    private int dims;
}
