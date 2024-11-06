package io.huskit.common;

import io.huskit.common.function.*;
import lombok.*;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

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

@NoArgsConstructor
@AllArgsConstructor
@ToString(of = "value")
@EqualsAndHashCode(of = "value")
final class DfVolatile<T> implements Volatile<T>, Serializable {

    @Nullable
    @NonFinal
    private volatile T value;

    public DfVolatile(Volatile<T> another) {
        another.ifPresent(this::set);
    }

    @Override
    public void set(T value) {
        this.value = Objects.requireNonNull(value, "value");
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
    @SneakyThrows
    public boolean check(ThrowingPredicate<T> predicate) {
        var val = value;
        return val != null && predicate.test(val);
    }

    @Override
    public Optional<T> maybe() {
        return Optional.ofNullable(value);
    }

    @Override
    public T or(T other) {
        var val = value;
        return val != null ? val : Objects.requireNonNull(other, "other");
    }

    @Override
    @SneakyThrows
    public T or(ThrowingSupplier<T> supplier) {
        var val = value;
        return val != null ? val : Objects.requireNonNull(supplier.get(), "supplier");
    }

    @Override
    @SneakyThrows
    public <R> R mapOr(ThrowingFunction<T, R> mapper, ThrowingSupplier<R> other) {
        var val = value;
        return val != null ? Objects.requireNonNull(mapper.apply(val), "mapper") : Objects.requireNonNull(other.get(), "other");
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
    @SneakyThrows
    public void ifPresentOrElse(ThrowingConsumer<T> consumer, ThrowingRunnable runnable) {
        var val = value;
        if (val != null) {
            consumer.accept(val);
        } else {
            runnable.run();
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
