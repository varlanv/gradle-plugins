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

    void ifInitialized(ThrowingConsumer<T> consumer);

    void ifNotInitialized(ThrowingRunnable runnable);

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
    @SneakyThrows
    public void ifInitialized(ThrowingConsumer<T> consumer) {
        var strategy = memoizedStrategy;
        if (strategy != null) {
            consumer.accept(strategy.get());
        }
    }

    @Override
    @SneakyThrows
    public void ifNotInitialized(ThrowingRunnable runnable) {
        if (memoizedStrategy == null) {
            runnable.run();
        }
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public void reset() {
        memoizedStrategy = null;
    }
}
