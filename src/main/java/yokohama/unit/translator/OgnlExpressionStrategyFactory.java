package yokohama.unit.translator;

public class OgnlExpressionStrategyFactory implements ExpressionStrategyFactory {
    @Override
    public ExpressionStrategy create(String name, String packageName) {
        return new OgnlExpressionStrategy(name, packageName);
    }
    
}
