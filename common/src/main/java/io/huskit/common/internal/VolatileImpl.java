package io.huskit.common.internal;

import io.huskit.common.Volatile;
import io.huskit.common.function.ThrowingConsumer;
import io.huskit.common.function.ThrowingSupplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VolatileImpl<T> implements Volatile<T>, Serializable {

    @Nullable
    @NonFinal
    private volatile T value;

    public VolatileImpl(Volatile<T> another) {
        another.ifPresent(this::set);
    }

    @Override
    public void set(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    @SneakyThrows
    public T syncSetOrGet(ThrowingSupplier<T> valueSupplier) {
        var val = this.value;
        if (val == null) {
            synchronized (this) {
                val = this.value;
                if (val == null) {
                    val = Objects.requireNonNull(valueSupplier.get());
                    this.value = val;
                }
            }
        }
        return val;
    }

    @SuppressWarnings("PMD.NullAssignment")
    public void reset() {
        this.value = null;
    }

    @Override
    public boolean isPresent() {
        return value != null;
    }

    @Override
    public Optional<T> maybe() {
        return Optional.ofNullable(value);
    }

    @Override
    @SneakyThrows
    public void ifPresent(ThrowingConsumer<T> consumer) {
        var val = value;
        if (val != null) {
            consumer.accept(val);
        }
    }

    @Override
    public T require() {
        var val = value;
        if (val != null) {
            return val;
        }
        throw new NoSuchElementException("No value present");
    }

    @Nullable
    public T get() {
        return value;
    }
}
