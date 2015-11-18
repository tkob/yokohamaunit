package yokohama.unit.translator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import yokohama.unit.annotations.As;

public class DataConverterFinder {
    public Optional<Method> find(Class<?> returnType, ClassLoader classLoader) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
        scanner.addIncludeFilter(new AnnotationTypeFilter(As.class));

        return scanner.findCandidateComponents("").stream()
            .map(BeanDefinition::getBeanClassName)
            .flatMap(className -> {
                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    return Arrays.<Method>asList(clazz.getMethods()).stream()
                            .filter(method -> true &&
                                    method.getReturnType().equals(returnType) &&
                                    method.getParameterCount() == 1 &&
                                    method.getParameterTypes()[0].equals(String.class));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).findFirst();
    }
}
