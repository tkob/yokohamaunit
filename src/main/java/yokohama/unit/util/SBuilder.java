package yokohama.unit.util;

import lombok.Getter;
import org.apache.commons.lang3.text.StrBuilder;
import yokohama.unit.annotations.Invariant;

@Invariant("{ it.indent >= 0 }")
public class SBuilder {

    private final StrBuilder sb = new StrBuilder();
    private final int shiftWidth;

    @Getter
    private int indent = 0;

    public SBuilder(int shiftWidth) {
        this.shiftWidth = shiftWidth;
    }
    
    public SBuilder appendln(final Object... array) {
        if (array.length > 0) {
            sb.appendPadding(indent, ' ');
            for (Object obj : array) {
                sb.append(obj);
            }
        }
        sb.appendNewLine();
        return this;
    }

    public SBuilder shift() {
        indent += shiftWidth;
        return this;
    }
    
    public SBuilder unshift() {
        indent = indent < shiftWidth ? 0 : indent - shiftWidth;
        return this;
    }
    
    @Override
    public String toString() {
        return sb.toString();
    }
}
