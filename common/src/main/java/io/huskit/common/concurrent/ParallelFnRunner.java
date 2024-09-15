package io.huskit.common.concurrent;

import io.huskit.common.Nothing;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class ParallelFnRunner<T, R> {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    List<Supplier<T>> suppliers;
    Duration timeout;

    public ParallelFnRunner(List<Supplier<T>> suppliers) {
        this(suppliers, TIMEOUT);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public List<R> doParallel(Function<T, R> function) {
        var size = suppliers.size();
        if (size == 1) {
            var value = suppliers.get(0).get();
            return Collections.singletonList(function.apply(value));
        } else if (size > 1) {
            var results = new Object[size];
            var futures = new CompletableFuture<?>[size];
            for (var i = 0; i < size; i++) {
                final var idx = i;
                futures[idx] = CompletableFuture.runAsync(() -> {
                    var value = suppliers.get(idx).get();
                    results[idx] = function.apply(value);
                });
            }
            CompletableFuture.allOf(futures).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return Arrays.asList((R[]) results);
        } else {
            return List.of();
        }
    }

    @SneakyThrows
    public void doParallel(Consumer<T> consumer) {
        doParallel(value -> {
            consumer.accept(value);
            return Nothing.instance();
        });
    }
}
