package io.huskit.common;

import io.huskit.common.function.ThrowingSupplier;
import io.huskit.common.internal.VolatileImpl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

public interface Volatile<T> {

    @Nullable
    T get();

    void set(T value);

    T syncSetOrGet(ThrowingSupplier<T> valueSupplier);

    void reset();

    default boolean isPresent() {
        return get() != null;
    }

    default Optional<T> maybe() {
        return Optional.ofNullable(get());
    }

    default T require() {
        return maybe().orElseThrow();
    }

    static <T> Volatile<T> of(T value) {
        return new VolatileImpl<>(value);
    }

    static <T> Volatile<T> of() {
        return new VolatileImpl<>();
    }
}
