package io.huskit.common.function;

import io.huskit.common.internal.DfVolatile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@RequiredArgsConstructor
public class StrategyMemoizedSupplier<T> implements MemoizedSupplier<T> {

    @NonNull
    ThrowingSupplier<ThrowingSupplier<T>> delegate;
    DfVolatile<ThrowingSupplier<T>> volatileValue = new DfVolatile<>();

    @Override
    @SneakyThrows
    public T get() {
        @Nullable var val = volatileValue.get();
        if (val == null) {
            synchronized (this) {
                val = volatileValue.get();
                if (val == null) {
                    val = Objects.requireNonNull(delegate.get(), "Supplier returned null strategy");
                    volatileValue.set(val);
                }
            }
        }
        return Objects.requireNonNull(val.get(), "Strategy returned null value");
    }

    @Override
    public boolean isInitialized() {
        return volatileValue.isPresent();
    }

    @Override
    public void reset() {
        volatileValue.reset();
    }
}
