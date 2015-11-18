package yokohama.unit.contract;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import static org.junit.Assert.assertEquals;
import yokohama.unit.annotations.Invariant;

public class GroovyContract implements Contract {
    @Override
    public void assertSatisfied(Object obj) {
        Class<?> clazz = obj.getClass();
        Invariant[] invariants = clazz.getAnnotationsByType(Invariant.class);
        for (Invariant invariant : invariants) {
            if (!"groovy".equalsIgnoreCase(invariant.lang())) continue;

            String condition = invariant.value();
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(binding);
            binding.setVariable("obj", obj);
            binding.setVariable("invariant", shell.evaluate(condition));
            Object result = shell.evaluate("invariant(obj)");
            assertEquals(condition, true, result);
        }
    }
}