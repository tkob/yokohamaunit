package yokohama.unit.ast;

import java.util.function.Function;

public interface StubBehavior {
    <T> T accept(StubBehaviorVisitor<T> visitor);

    default <T> T accept(
            Function<StubReturns, T> visitStubReturns_,
            Function<StubThrows, T> visitStubThrows_
    ) {
        return accept(new StubBehaviorVisitor<T>() {
            @Override
            public T visitStubReturns(StubReturns stubReturns) {
                return visitStubReturns_.apply(stubReturns);
            }

            @Override
            public T visitStubThrows(StubThrows stubThrows) {
                return visitStubThrows_.apply(stubThrows);
            }
        });
    }
}
