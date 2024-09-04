package io.huskit.gradle.commontest;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileDeleteStrategy;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface BaseTest {

    int DEFAULT_REPEAT_COUNT = 10;

    default <T> T parseJson(String json, Class<T> type) {
        return (T) null;
    }

    default <T> T getJsonField(String json, String field, Class<T> type) {
        return JsonUtil.getJsonField(json, field, type);
    }

    @SneakyThrows
    default void parallel(int nThreads, ThrowingRunnable runnable) {
        var executorService = Executors.newFixedThreadPool(nThreads);
        var readyToStartLock = new CountDownLatch(nThreads);
        var startLock = new CountDownLatch(1);
        var finishedLock = new CountDownLatch(nThreads);

        for (var i = 0; i < nThreads; i++) {
            executorService.submit(() -> {
                try {
                    readyToStartLock.countDown();
                    startLock.await(5, TimeUnit.SECONDS);  // Wait without a timeout
                    runnable.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    finishedLock.countDown();
                }
            });
        }

        readyToStartLock.await();  // Wait for all threads to be ready
        startLock.countDown(); // Signal all threads to start
        finishedLock.await(5, TimeUnit.SECONDS); // Wait for all threads to finish
        executorService.shutdownNow();
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
                    throw new RuntimeException(originalException);
                }
            } catch (IOException e) {
                throw new RuntimeException(Objects.requireNonNullElse(originalException, e));
            }
        }
    }

    interface ThrowingRunnable {
        void run() throws Exception;
    }

    interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }
}
