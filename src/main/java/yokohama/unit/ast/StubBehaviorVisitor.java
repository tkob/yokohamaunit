package yokohama.unit.ast;

public interface StubBehaviorVisitor<T> {
    T visitStubReturns(StubReturns stubReturns);
    T visitStubThrows(StubThrows stubThrows);
}
