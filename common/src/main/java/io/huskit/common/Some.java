package io.huskit.common;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
class Some<T> implements Opt<T> {

    @NonNull
    T value;

    @Override
    public T require(String missingValueMessage) {
        return value;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
        consumer.accept(value);
    }
}
