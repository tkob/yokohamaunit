package yokohama.unit.translator;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GroovyContract implements Contract {
    @Override
    public void assertSatisfied(Object obj, String condition) {
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        binding.setVariable("obj", obj);
        binding.setVariable("invariant", shell.evaluate(condition));
        Object result = shell.evaluate("invariant(obj)");
        assertEquals(condition, true, result);
    }
    
    @Test
    public void test() {
        assertSatisfied("helloworld", "({ it.startsWith(\"hello\") })");
    }
}
