package yokohama.unit.position;

import lombok.Value;

@Value
public class Position {
    private int line;
    private int column;

    private static Position dummyPos = new Position(-1, -1);
    public static Position dummyPos() {
        return dummyPos;
    }
}
