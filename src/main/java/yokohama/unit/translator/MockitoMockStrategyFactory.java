package yokohama.unit.translator;

public class MockitoMockStrategyFactory implements MockStrategyFactory {
    @Override
    public MockStrategy create() {
        return new MockitoMockStrategy();
    }
    
}
