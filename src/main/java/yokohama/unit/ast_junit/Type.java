package yokohama.unit.ast_junit;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class Type {
    private NonArrayType nonArrayType;
    private int dims;

    public String getText() {
        return nonArrayType.getText() + StringUtils.repeat("[]", dims);
    }
}
