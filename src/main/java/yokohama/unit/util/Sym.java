package yokohama.unit.util;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "of")
public class Sym {
    private String name;
}
