package io.huskit.common;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public interface Volatile<T> {

    @Nullable
    T get();

    void set(T value);

    T syncSetOrGet(Supplier<T> valueSupplier);

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
