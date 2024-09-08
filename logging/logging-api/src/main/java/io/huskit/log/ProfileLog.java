package io.huskit.log;

import io.huskit.common.function.ThrowingRunnable;
import io.huskit.common.function.ThrowingSupplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileLog<T> implements Runnable, Supplier<T> {

    Log log;
    String description;
    ThrowingSupplier<T> delegate;

    ProfileLog(Log log, String description, ThrowingRunnable delegate) {
        this(log, description, () -> {
            delegate.run();
            return null;
        });
    }

    @Override
    public void run() {
        get();
    }

    @Override
    @SneakyThrows
    public T get() {
        var time = System.currentTimeMillis();
        T result = delegate.get();
        log.error("[{}] exec time: [{}]", description, Duration.ofMillis(System.currentTimeMillis() - time));
        return result;
    }

    public static <T> T withProfile(String description, ThrowingSupplier<T> supplier) {
        return new ProfileLog<>(new Slf4jLog(), description, supplier).get();
    }

    public static void withProfile(String description, ThrowingRunnable runnable) {
        new ProfileLog<>(new Slf4jLog(), description, runnable).run();
    }
}
