package yokohama.unit.translator;

import yokohama.unit.util.GenSym;

public class MockitoMockStrategyFactory implements MockStrategyFactory {
    @Override
    public MockStrategy create(GenSym genSym) {
        return new MockitoMockStrategy(genSym);
    }
    
}
