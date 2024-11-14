package io.huskit.common.concurrent;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@UtilityClass
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
