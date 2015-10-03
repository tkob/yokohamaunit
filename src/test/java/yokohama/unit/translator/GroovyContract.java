package yokohama.unit.translator;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.Getter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
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

    @Test
    public void test() {
        @Invariant("({ it.count >= 0 })")
        class Counter {
            @Getter
            private int count = 0;
            public void incr() {
                count++;
            }
        }
        Counter counter = new Counter();
        assertSatisfied(counter);
        counter.incr();
        assertSatisfied(counter);
    }
}
