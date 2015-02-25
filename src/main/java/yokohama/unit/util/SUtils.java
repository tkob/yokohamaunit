package yokohama.unit.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import javax.lang.model.SourceVersion;

public class SUtils {
    public static final String toIdent(final String text) {
        final StringBuilder sb = new StringBuilder(text.length() + 1);

        // an automaton with three states: Start, Part and UnderScore.
        CharacterConsumer ctx = new CharacterConsumer() {
            private final CharacterConsumer START = new Start();
            private final CharacterConsumer PART = new Part();
            private final CharacterConsumer UNDERSCORE = new UnderScore();
            private CharacterConsumer state = START;

            @Override
            public void accept(char ch) {
                state.accept(ch);
            }

            @Override
            public String toString() {
                return sb.toString();
            }

            class Start implements CharacterConsumer {
                @Override
                public void accept(char ch) {
                    if (Character.isJavaIdentifierStart(ch)) {
                        sb.append(ch);
                    } else {
                        sb.append('_');
                        sb.append(ch);
                    }
                    state = PART;
                }
            };

            class Part implements CharacterConsumer {
                @Override
                public void accept(char ch) {
                    if (Character.isJavaIdentifierPart(ch)) {
                        sb.append(ch);
                    } else {
                        state = UNDERSCORE;
                    }
                }
            };

            class UnderScore implements CharacterConsumer {
                @Override
                public void accept(char ch) {
                    if (Character.isJavaIdentifierPart(ch)) {
                        sb.append('_');
                        sb.append(ch);
                        state = PART;
                    }
                }
            };
        };

        StringCharacterIterator iter = new StringCharacterIterator(text);
        for (char ch = iter.first(); ch != CharacterIterator.DONE; ch = iter.next()) {
            ctx.accept(ch);
        }

        if (SourceVersion.isKeyword(sb)) {
            sb.append('_');
        }

        return ctx.toString();
    }
}
