package io.huskit.common.function;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.Nullable;

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
public interface MemoizedSupplier<T> extends Supplier<T>, ThrowingSupplier<T> {

    @Override
    T get();

    boolean isInitialized();

    void reset();

    static <T> MemoizedSupplier<T> of(ThrowingSupplier<T> supplier) {
        return new StrategyMemoizedSupplier<>(() -> {
            try {
                var val = supplier.get();
                return () -> val;
            } catch (Exception e) {
                return () -> {
                    throw e;
                };
            }
        });
    }

    static <T> MemoizedSupplier<T> ofStrategy(ThrowingSupplier<ThrowingSupplier<T>> supplier) {
        return new StrategyMemoizedSupplier<>(supplier);
    }

    static <T> MemoizedSupplier<T> ofLocal(ThrowingSupplier<T> supplier) {
        return new LocalMemoizedSupplier<>(supplier);
    }
}

@RequiredArgsConstructor
final class LocalMemoizedSupplier<T> implements MemoizedSupplier<T> {

    @NonNull
    ThrowingSupplier<T> delegate;
    @NonFinal
    @Nullable
    T value;

    @Override
    @SneakyThrows
    public T get() {
        var value = this.value;
        if (value == null) {
            value = Objects.requireNonNull(this.delegate.get(), "Supplier returned null value");
            this.value = value;
        }
        return value;
    }

    @Override
    public boolean isInitialized() {
        return value != null;
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public void reset() {
        value = null;
    }
}

@RequiredArgsConstructor
final class StrategyMemoizedSupplier<T> implements MemoizedSupplier<T> {

    @NonNull
    ThrowingSupplier<ThrowingSupplier<T>> delegate;
    @NonFinal
    @Nullable
    volatile ThrowingSupplier<T> memoizedStrategy;

    @Override
    @SneakyThrows
    public T get() {
        @Nullable var strategy = memoizedStrategy;
        if (strategy == null) {
            synchronized (this) {
                strategy = memoizedStrategy;
                if (strategy == null) {
                    strategy = Objects.requireNonNull(delegate.get(), "Supplier returned null strategy");
                    memoizedStrategy = strategy;
                }
            }
        }
        return Objects.requireNonNull(strategy.get(), "Supplier returned null value");
    }

    @Override
    public boolean isInitialized() {
        return memoizedStrategy != null;
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public void reset() {
        memoizedStrategy = null;
    }
}
