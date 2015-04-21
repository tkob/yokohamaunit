package yokohama.unit.translator;

public class OgnlExpressionStrategyFactory implements ExpressionStrategyFactory {
    @Override
    public ExpressionStrategy create() {
        return new OgnlExpressionStrategy();
    }
    
}
