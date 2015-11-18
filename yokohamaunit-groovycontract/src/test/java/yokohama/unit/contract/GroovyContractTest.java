package yokohama.unit.contract;

import org.junit.Test;
import yokohama.unit.annotations.Invariant;

public class GroovyContractTest {
    @Test
    public void test() {
        @Invariant("({ it.count >= 0 })")
        class Counter {
            private int count = 0;
            public int getCount() {
                return count;
            }
            public void incr() {
                count++;
            }
        }
        Counter counter = new Counter();
        new GroovyContract().assertSatisfied(counter);
        counter.incr();
        new GroovyContract().assertSatisfied(counter);
    }
}