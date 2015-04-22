package yokohama.unit.translator;

public interface ExpressionStrategyFactory {
    /**
     * 
     * @param name        the name of the docy source (without extension)
     * @param packageName the package name, inferred from docy source path
     * @return 
     */
    ExpressionStrategy create(String name, String packageName);
}
