package io.huskit.common.function;

import io.huskit.common.internal.DfVolatile;
import lombok.RequiredArgsConstructor;

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
public class MemoizedSupplier<T> implements Supplier<T>, ThrowingSupplier<T> {

    Supplier<T> delegate;
    DfVolatile<T> volatileValue = new DfVolatile<>();

    @Override
    public T get() {
        var val = volatileValue.get();
        if (val == null) {
            synchronized (this) {
                val = volatileValue.get();
                if (val == null) {
                    val = Objects.requireNonNull(delegate.get());
                    volatileValue.set(val);
                }
            }
        }
        return val;
    }

    public boolean isInitialized() {
        return volatileValue.isPresent();
    }

    public void reset() {
        volatileValue.reset();
    }
}
