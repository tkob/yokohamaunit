package yokohama.unit.ast_junit;

import lombok.Value;
import yokohama.unit.util.SBuilder;

@Value
public class ImportClass implements ImportedName {
    private final String name;

    @Override
    public void toString(SBuilder sb) {
        sb.appendln("import ", name, ";");
    }
}
