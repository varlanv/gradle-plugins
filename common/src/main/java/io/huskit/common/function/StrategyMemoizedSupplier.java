package io.huskit.common.function;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@RequiredArgsConstructor
public class StrategyMemoizedSupplier<T> implements MemoizedSupplier<T> {

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
    public void reset() {
        memoizedStrategy = null;
    }
}
