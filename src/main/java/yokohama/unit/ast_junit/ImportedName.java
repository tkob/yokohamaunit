package yokohama.unit.ast_junit;

import yokohama.unit.util.SBuilder;

public interface ImportedName extends Comparable<ImportedName> {
    void toString(SBuilder sb);

    String getName();

    @Override
    default int compareTo(ImportedName that) {
        return this.getName().compareTo(that.getName());
    }
    
}
