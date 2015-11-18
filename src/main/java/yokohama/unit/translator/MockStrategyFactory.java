package yokohama.unit.translator;

import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;

public interface MockStrategyFactory {
    MockStrategy create(
            String name, String packageName, GenSym genSym, ClassResolver classResolver);
}
