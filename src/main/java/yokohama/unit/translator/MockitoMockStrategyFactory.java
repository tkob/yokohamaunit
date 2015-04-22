package yokohama.unit.translator;

import yokohama.unit.util.GenSym;

public class MockitoMockStrategyFactory implements MockStrategyFactory {
    @Override
    public MockStrategy create(String name, String packageName, GenSym genSym) {
        return new MockitoMockStrategy(name, packageName, genSym);
    }
    
}
