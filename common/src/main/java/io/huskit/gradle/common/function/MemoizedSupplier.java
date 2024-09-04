package io.huskit.gradle.common.function;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A supplier that remembers the result of its computation.
 * It is thread-safe and guarantees that the computation is performed at most once.
 * <p>
 * <b>Turns out gradle configuration cache has poor support for storing suppliers as instance fields, so this class
 * has limited use.</b>
 *
 * @param <T> the type of results supplied by this supplier
 */
@RequiredArgsConstructor
public class MemoizedSupplier<T> implements Supplier<T> {

    Supplier<T> delegate;
    volatile @NonFinal T value;

    @Override
    public T get() {
        var val = value;
        if (val == null) {
            synchronized (this) {
                val = value;
                if (val == null) {
                    val = Objects.requireNonNull(delegate.get());
                    value = val;
                }
            }
        }
        return val;
    }

    public boolean isInitialized() {
        return value != null;
    }

    public void reset() {
        value = null;
    }
}
