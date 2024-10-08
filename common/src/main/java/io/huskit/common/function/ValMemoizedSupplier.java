package io.huskit.common.function;

import io.huskit.common.internal.DfVolatile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Objects;

@RequiredArgsConstructor
public class ValMemoizedSupplier<T> implements MemoizedSupplier<T> {

    @NonNull
    ThrowingSupplier<T> delegate;
    DfVolatile<T> volatileValue = new DfVolatile<>();

    @Override
    @SneakyThrows
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

    @Override
    public boolean isInitialized() {
        return volatileValue.isPresent();
    }

    @Override
    public void reset() {
        volatileValue.reset();
    }
}
