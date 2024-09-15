package io.huskit.common;

import lombok.NoArgsConstructor;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class None<T> implements Opt<T> {

    private static final None<?> INSTANCE = new None<>();

    @SuppressWarnings("unchecked")
    static <T> None<T> instance() {
        return (None<T>) INSTANCE;
    }

    @Override
    public T require(String missingValueMessage) {
        throw new NoSuchElementException(missingValueMessage);
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
        // no-op
    }
}
