package io.huskit.gradle.commontest;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileDeleteStrategy;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.setMaxStackTraceElementsDisplayed;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface BaseTest {

    String DOCKER_TEST_TAG = "docker-test";
    String SLOW_TEST_TAG = "slow-test";
    String FAST_TEST_TAG = "fast-test";
    String UNIT_TEST_TAG = "unit-test";
    String INTEGRATION_TEST_TAG = "integration-test";
    String FUNCTIONAL_TEST_TAG = "functional-test";
    int DEFAULT_REPEAT_COUNT = 10;

    @BeforeAll
    default void setupAssertjParent() {
        setMaxStackTraceElementsDisplayed(Integer.MAX_VALUE);
    }

    default <T> T parseJson(String json, Class<T> type) {
        return (T) null;
    }

    default <T> T getJsonField(String json, String field, Class<T> type) {
        return JsonUtil.getJsonField(json, field, type);
    }

    @SneakyThrows
    default void parallel(int threads, ThrowingRunnable runnable) {
        var executorService = Executors.newFixedThreadPool(threads);
        var exceptionRef = new AtomicReference<Exception>();
        try {
            var readyToStartLock = new CountDownLatch(threads);
            var startLock = new CountDownLatch(1);
            var finishedLock = new CountDownLatch(threads);

            for (var i = 0; i < threads; i++) {
                executorService.submit(() -> {
                    try {
                        readyToStartLock.countDown();
                        startLock.await(5, TimeUnit.SECONDS);  // Wait without a timeout
                        runnable.run();
                    } catch (Exception e) {
                        exceptionRef.set(e);
                    } finally {
                        finishedLock.countDown();
                    }
                });
            }

            readyToStartLock.await();  // Wait for all threads to be ready
            startLock.countDown(); // Signal all threads to start
            finishedLock.await(5, TimeUnit.SECONDS); // Wait for all threads to finish
        } finally {
            executorService.shutdown();
        }
        if (exceptionRef.get() != null) {
            throw exceptionRef.get();
        }
    }

    default void parallel(ThrowingRunnable runnable) {
        parallel(DEFAULT_REPEAT_COUNT, runnable);
    }

    @SneakyThrows
    default File newTempDir() {
        var dir = Files.createTempDirectory("huskitjunit-").toFile();
        dir.deleteOnExit();
        return dir;
    }

    @SneakyThrows
    default Path newTempFile() {
        return Files.createTempFile("huskitjunit-", ".tmp");
    }

    @SneakyThrows
    default void useTempFile(ThrowingConsumer<Path> action) {
        var file = newTempFile();
        try {
            action.accept(file);
        } finally {
            if (Files.exists(file)) {
                Files.delete(file);
            }
        }
    }

    default void runAndDeleteFile(@NonNull File file, ThrowingRunnable runnable) {
        Exception originalException = null;
        try {
            runnable.run();
        } catch (Exception e) {
            originalException = e;
        } finally {
            try {
                FileDeleteStrategy.FORCE.delete(file);
                if (originalException != null) {
                    rethrow(originalException);
                }
            } catch (IOException e) {
                rethrow(Objects.requireNonNullElse(originalException, e));
            }
        }
    }

    default Condition<? super Instant> today() {
        return new Condition<>(instant -> {
            var now = Instant.now();
            var start = now.truncatedTo(TimeUnit.DAYS.toChronoUnit());
            var end = start.plus(1, TimeUnit.DAYS.toChronoUnit());
            return !instant.isBefore(start) && !instant.isAfter(end);
        }, "today");
    }

    default <T extends Throwable> void rethrow(Throwable t) {
        throw hide(t);
    }

    @SuppressWarnings("unchecked")
    default <T extends Throwable> T hide(Throwable t) throws T {
        throw (T) t;
    }

    interface ThrowingRunnable {
        void run() throws Exception;
    }

    interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    @SneakyThrows
    default void microBenchmark(ThrowingSupplier<?> action) {
        microBenchmark(100, "", action);
    }

    @SneakyThrows
    default void microBenchmark(String message, ThrowingSupplier<?> action) {
        microBenchmark(100, message, action);
    }

    @SneakyThrows
    default void microBenchmark(Integer iterations, ThrowingSupplier<?> action) {
        microBenchmark(iterations, "", action);
    }

    @SneakyThrows
    default void microBenchmark(Integer iterations, String message, ThrowingSupplier<?> action) {
        assertThat(action.get()).isNotNull();

        var nanosBefore = 0L;
        var average = 0L;
        for (var i = 0; i < iterations; i++) {
            nanosBefore = System.nanoTime();
            assertThat(action.get()).matches(it -> true);
            average += System.nanoTime() - nanosBefore;
        }
        average /= iterations;
        var msg = message.isEmpty() ? message : message + " ";
        var avgMicros = average / 1_000;
        if (avgMicros > 0 && avgMicros < 10000) {
            System.out.printf("%nTime micros %s-> %d%n%n", msg, (average / 1_000));
        } else if (avgMicros == 0) {
            System.out.printf("%nTime nanos %s-> %d%n%n", msg, average);
        } else {
            System.out.printf("%nTime millis %s-> %d%n%n", msg, (average / 1_000_000));
        }
    }
}
