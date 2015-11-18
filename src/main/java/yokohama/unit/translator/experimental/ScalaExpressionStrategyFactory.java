package yokohama.unit.translator.experimental;

import yokohama.unit.translator.ExpressionStrategy;
import yokohama.unit.translator.ExpressionStrategyFactory;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;

public class ScalaExpressionStrategyFactory implements ExpressionStrategyFactory {
    @Override
    public ExpressionStrategy create(
            String name,
            String packageName,
            GenSym genSym,
            ClassResolver classResolver) {
        return new ScalaExpressionStrategy(
                name, packageName, genSym, classResolver);
    }
}
