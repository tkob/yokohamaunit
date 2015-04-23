package yokohama.unit.translator;

import yokohama.unit.util.GenSym;

public class OgnlExpressionStrategyFactory implements ExpressionStrategyFactory {
    @Override
    public ExpressionStrategy create(String name, String packageName, GenSym genSym) {
        return new OgnlExpressionStrategy(name, packageName, genSym);
    }
    
}
