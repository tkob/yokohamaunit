package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ImportStatic implements ImportedName {
    private final String name;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("import static ", name, ";");
    }
}
