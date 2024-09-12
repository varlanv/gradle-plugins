package io.huskit.common.internal;

import io.huskit.common.Volatile;
import io.huskit.common.function.ThrowingSupplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VolatileImpl<T> implements Volatile<T>, Serializable {

    @Nullable
    @NonFinal
    private volatile T value;

    @Override
    public @Nullable T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    @SneakyThrows
    public T syncSetOrGet(ThrowingSupplier<T> valueSupplier) {
        var val = this.value;
        if (val == null) {
            synchronized (this) {
                val = this.value;
                if (val == null) {
                    val = valueSupplier.get();
                    this.value = val;
                }
            }
        }
        return val;
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public void reset() {
        this.value = null;
    }
}
