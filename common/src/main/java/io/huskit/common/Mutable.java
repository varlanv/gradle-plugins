package io.huskit.common;

import io.huskit.common.function.ThrowingConsumer;
import io.huskit.common.function.ThrowingPredicate;
import io.huskit.common.function.ThrowingSupplier;
import io.huskit.common.internal.DfMutable;

import java.util.Optional;

public interface Mutable<T> {

    static <T> Mutable<T> of(T value) {
        return new DfMutable<>(value);
    }

    static <T> Mutable<T> of() {
        return new DfMutable<>();
    }

    void set(T value);

    boolean isPresent();

    boolean check(ThrowingPredicate<T> predicate);

    Optional<T> maybe();

    T or(T other);

    T or(ThrowingSupplier<T> supplier);

    void ifPresent(ThrowingConsumer<T> consumer);

    T require();

    default boolean isEmpty() {
        return !isPresent();
    }
}
