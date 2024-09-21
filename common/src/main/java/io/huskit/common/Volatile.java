package io.huskit.common;

import io.huskit.common.function.ThrowingSupplier;
import io.huskit.common.internal.DfVolatile;

public interface Volatile<T> extends Mutable<T> {

    T syncSetOrGet(ThrowingSupplier<T> valueSupplier);

    static <T> Volatile<T> of(Volatile<T> another) {
        return new DfVolatile<>(another);
    }

    static <T> Volatile<T> of(T value) {
        return new DfVolatile<>(value);
    }

    static <T> Volatile<T> of() {
        return new DfVolatile<>();
    }
}
