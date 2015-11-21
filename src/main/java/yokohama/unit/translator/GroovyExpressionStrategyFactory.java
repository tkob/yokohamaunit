package yokohama.unit.translator;

import java.lang.reflect.Method;
import java.util.List;
import yokohama.unit.util.ClassResolver;
import yokohama.unit.util.GenSym;

public class GroovyExpressionStrategyFactory implements ExpressionStrategyFactory {
    @Override
    public ExpressionStrategy create(
            String name,
            String packageName,
            GenSym genSym,
            ClassResolver classResolver,
            List<String> converterBasePackages) {
        DataConverterFinder finder = new DataConverterFinder();
        ClassLoader classLoader = classResolver.getClassLoader();
        List<Method> asMethods = finder.find(classLoader, converterBasePackages);
        return new GroovyExpressionStrategy(
                name, packageName, genSym, classResolver, asMethods);
    }
}
