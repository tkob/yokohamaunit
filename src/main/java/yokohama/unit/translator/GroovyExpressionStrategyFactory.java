package yokohama.unit.translator;

import yokohama.unit.util.GenSym;

public class GroovyExpressionStrategyFactory implements ExpressionStrategyFactory {
    @Override
    public ExpressionStrategy create(String name, String packageName, GenSym genSym) {
        return new GroovyExpressionStrategy(name, packageName, genSym);
    }
}
