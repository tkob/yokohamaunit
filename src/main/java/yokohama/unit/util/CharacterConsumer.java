package yokohama.unit.util;

import lombok.NonNull;

@FunctionalInterface
public interface CharacterConsumer {
    /**
     * Performs this operation on the given argument.
     *
     * @param ch the input argument
     */
    void accept(char ch);

    /**
     * Returns a composed {@code CharacterConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code CharacterConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default CharacterConsumer andThen(@NonNull CharacterConsumer after) {
        return (char ch) -> { accept(ch); after.accept(ch); };
    }
    
}
