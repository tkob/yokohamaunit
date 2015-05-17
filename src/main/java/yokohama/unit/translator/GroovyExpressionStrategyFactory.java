package yokohama.unit.translator;

import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;

public class GroovyExpressionStrategyFactory implements ExpressionStrategyFactory {
    @Override
    public ExpressionStrategy create(
            String name,
            String packageName,
            GenSym genSym,
            ClassResolver classResolver) {
        return new GroovyExpressionStrategy(
                name, packageName, genSym, classResolver);
    }
}
