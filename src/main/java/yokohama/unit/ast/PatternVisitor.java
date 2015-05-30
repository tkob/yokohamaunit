package yokohama.unit.ast;

public interface PatternVisitor<T> {
    T visitRegExpPattern(RegExpPattern regExpPattern);
}
