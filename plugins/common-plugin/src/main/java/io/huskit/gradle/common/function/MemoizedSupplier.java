package io.huskit.gradle.common.function;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class MemoizedSupplier<T> implements Supplier<T> {

    Supplier<T> delegate;
    @NonFinal
    volatile T value;

    @Override
    public T get() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = delegate.get();
                }
            }
        }
        return value;
    }
}
