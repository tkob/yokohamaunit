package yokohama.unit.ast;

import lombok.Value;

@Value
public class Position {
    private int line;
    private int column;
}
