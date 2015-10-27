package yokohama.unit.translator;

import lombok.Getter;
import yokohama.unit.annotations.Invariant;

@lombok.AllArgsConstructor
@Invariant("({ it.count >= 0 })")
@Invariant("({ it.count < 10})")
public class DummyCounter {
    @Getter
    private int count;
    
    public void increment() {
        count++;
    }
}
