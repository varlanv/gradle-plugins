package io.huskit.common;

import io.huskit.common.function.ThrowingConsumer;
import io.huskit.common.function.ThrowingSupplier;
import io.huskit.common.internal.VolatileImpl;

import java.util.Optional;

public interface Volatile<T> {

    void set(T value);

    T syncSetOrGet(ThrowingSupplier<T> valueSupplier);

    boolean isPresent();

    Optional<T> maybe();

    void ifPresent(ThrowingConsumer<T> consumer);

    T require();

    default boolean isEmpty() {
        return !isPresent();
    }

    static <T> Volatile<T> of(Volatile<T> another) {
        return new VolatileImpl<>(another);
    }

    static <T> Volatile<T> of(T value) {
        return new VolatileImpl<>(value);
    }

    static <T> Volatile<T> of() {
        return new VolatileImpl<>();
    }
}
