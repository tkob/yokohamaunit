package yokohama.unit.util;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

public abstract class FList<E> {
    public abstract <T> T match(Supplier<T> visitNil, BiFunction<E, FList<E>, T> visitCons);
    public abstract int size();
    public abstract boolean isEmpty();
    public abstract boolean contains(Object obj);
    public abstract E get(int index);
    public FList<E> add(E obj) { return cons(obj, this); }

    @Value
    @EqualsAndHashCode(callSuper=false)
    private static class Nil<E> extends FList<E> {
        @Override
        public <T> T match(Supplier<T> visitNil, BiFunction<E, FList<E>, T> visitCons) {
            return visitNil.get();
        }
        @Override
        public int size() {
            return 0;
        }
        @Override
        public boolean isEmpty() {
            return true;
        }
        @Override
        public boolean contains(Object obj) {
            return false;
        }
        @Override
        public E get(int index) {
            throw new IndexOutOfBoundsException();
        }
    }
    @Value
    @EqualsAndHashCode(callSuper=false)
    private static class Cons<E> extends FList<E> {
        private E car;
        private @NonNull FList<E> cdr;

        @Override
        public <T> T match(Supplier<T> visitNil, BiFunction<E, FList<E>, T> visitCons) {
            return visitCons.apply(car, cdr);
        }
        @Override
        public int size() {
            return 1 + cdr.size();
        }
        @Override
        public boolean isEmpty() {
            return false;
        }
        @Override
        public boolean contains(Object obj) {
            if (car.equals(obj)) return true;
            else return cdr.contains(obj);
        }
        @Override
        public E get(int index) {
            if (index == 0) return car;
            else return cdr.get(index - 1);
        }
    }

    private static final Nil nil = new Nil();
    @SuppressWarnings("unchecked")
    public static <E> FList<E> empty() {
        return nil;
    }
    public static <E> FList<E> cons(E car, FList<E> cdr) {
        return new Cons<>(car, cdr);
    }
    public static <E> FList<E> append(FList<E> l1, FList<E> l2) {
        return l1.match(
                () -> l2,
                (car, cdr) -> cons(car, append(cdr, l2)));
    }
    public static <E> FList<E> of(E... objs) {
        FList<E> l = empty();
        for (int i = objs.length - 1; i >= 0; i--) {
            l = l.add(objs[i]);
        }
        return l;
    }
}
