package io.huskit.common.function;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
public class MemoizedFunction<T, R> implements Function<T, R> {

    Function<T, R> delegate;
    volatile @NonFinal R value;

    @Override
    public R apply(T t) {
        var val = value;
        if (val == null) {
            synchronized (this) {
                val = value;
                if (val == null) {
                    val = Objects.requireNonNull(delegate.apply(t));
                    value = val;
                }
            }
        }
        return val;
    }
}
