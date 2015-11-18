package yokohama.unit.ast;

import java.util.function.Function;

public interface Pattern {
    <T> T accept(PatternVisitor<T> visitor);
    
    default <T> T accept(
            Function<RegExpPattern, T> visitRegExpPattern_
    ) {
        return accept(new PatternVisitor<T>() {
            @Override
            public T visitRegExpPattern(RegExpPattern regExpPattern) {
                return visitRegExpPattern_.apply(regExpPattern);
            }
        });
    }
}
