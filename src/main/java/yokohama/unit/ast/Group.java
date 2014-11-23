package yokohama.unit.ast;

import java.util.List;
import lombok.Value;

@Value
public class Group {
    private List<Definition> definitions;
}
