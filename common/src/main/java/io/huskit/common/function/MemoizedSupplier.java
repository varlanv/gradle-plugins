package io.huskit.common.function;

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
public interface MemoizedSupplier<T> extends Supplier<T>, ThrowingSupplier<T> {

    T get();

    boolean isInitialized();

    void reset();

    static <T> MemoizedSupplier<T> of(ThrowingSupplier<T> supplier) {
        return new ValMemoizedSupplier<>(supplier);
    }

    static <T> MemoizedSupplier<T> ofStrategy(ThrowingSupplier<ThrowingSupplier<T>> supplier) {
        return new StrategyMemoizedSupplier<>(supplier);
    }
}
