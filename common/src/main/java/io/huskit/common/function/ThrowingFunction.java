package io.huskit.common.function;

import io.huskit.common.Sneaky;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;

    default Function<T, R> toUnchecked() {
        return t -> {
            try {
                return apply(t);
            } catch (Exception e) {
                throw Sneaky.rethrow(e);
            }
        };
    }
}
