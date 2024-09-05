package io.huskit.containers.model.started;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class ContainerLauncher<T, R> {

    private static final int TIMEOUT = 10;
    List<Supplier<T>> suppliers;

    @SneakyThrows
    public List<R> doParallel(Function<T, R> function) {
        var size = suppliers.size();
        if (size == 1) {
            var value = suppliers.get(0).get();
            return List.of(function.apply(value));
        } else if (size > 1) {
            var executor = Executors.newFixedThreadPool(size);
            var latch = new CountDownLatch(size);
            var results = new ArrayList<R>(size);
            for (var i = 0; i < size; i++) {
                final var idx = i;
                executor.submit(() -> {
                    var value = suppliers.get(idx).get();
                    results.set(idx, function.apply(value));
                    latch.countDown();
                });
            }
            executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
            executor.shutdown();
            return results;
        } else {
            return List.of();
        }
    }
}
