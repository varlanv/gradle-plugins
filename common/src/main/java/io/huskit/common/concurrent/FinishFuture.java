package io.huskit.common.concurrent;

import lombok.SneakyThrows;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FinishFuture {

    @SneakyThrows
    public static <T> T finish(CompletableFuture<T> future, Duration timeout) {
        if (timeout.isZero()) {
            return future.join();
        } else {
            return future.get(timeout.toSeconds(), TimeUnit.SECONDS);
        }
    }
}
