package yokohama.unit.translator;

import yokohama.unit.util.GenSym;

public class ElExpressionStrategyFactory implements ExpressionStrategyFactory {
    @Override
    public ExpressionStrategy create(String name, String packageName, GenSym genSym) {
        return new ElExpressionStrategy(name, packageName, genSym);
    }
}
