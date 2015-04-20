package yokohama.unit.position;

import lombok.Value;

@Value
public class Position {
    private int line;
    private int column;

    public boolean isDummy() {
        return line < 0 && column < 0;
    }

    public static Position of(int line) {
        return new Position(line, -1);
    }

    public static Position of(int line, int column) {
        return new Position(line, column);
    }

    private static Position dummyPos = new Position(-1, -1);
    public static Position dummyPos() {
        return dummyPos;
    }
}
