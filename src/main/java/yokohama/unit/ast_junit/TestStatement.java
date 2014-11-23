package yokohama.unit.ast_junit;

import java.util.Set;

public interface TestStatement extends Stringifiable {
    Set<ImportedName> importedNames();
}
