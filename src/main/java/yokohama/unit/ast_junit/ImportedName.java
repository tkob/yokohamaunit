package yokohama.unit.ast_junit;

public interface ImportedName extends Stringifiable, Comparable<ImportedName> {
    String getName();

    @Override
    default int compareTo(ImportedName that) {
        return this.getName().compareTo(that.getName());
    }
    
}
