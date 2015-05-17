package yokohama.unit.translator;

import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;

public interface ExpressionStrategyFactory {
    /**
     * 
     * @param name        the name of the docy source (without extension)
     * @param packageName the package name, inferred from docy source path
     * @param genSym      the symbol generator
     * @param classResolver
     * @return 
     */
    ExpressionStrategy create(
            String name,
            String packageName,
            GenSym genSym,
            ClassResolver classResolver);
}
