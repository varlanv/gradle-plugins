package io.huskit.containers.http;

import io.huskit.gradle.commontest.IntegrationTest;
import lombok.experimental.NonFinal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NpipeReadIntegrationTest implements IntegrationTest {

    @NonFinal
    ExecutorService executorService;

    @BeforeAll
    void setupAll() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @AfterAll
    void cleanupAll() {
        executorService.shutdownNow();
    }

    @Test
    void when_exception_happens_during_read__should_throw_exception() {
        var data = "Hello";
        var exceptionMessage = "Test exception";
        var futureResponse = errorFutureResponse(exceptionMessage);
        var bytesSupplier = getCompletableFutureSupplier(data);
        assertThatThrownBy(
                () -> new NpipeRead(
                        bytesSupplier,
                        executorService
                ).read(futureResponse).join()
        ).cause().hasMessage(exceptionMessage);
    }

    private Supplier<CompletableFuture<ByteBuffer>> getCompletableFutureSupplier(String data) {
        return () -> CompletableFuture.completedFuture(ByteBuffer.wrap(data.getBytes()));
    }

    private FutureResponse<String> errorFutureResponse(String exceptionMessage) {
        var counter = new AtomicInteger();
        return new FutureResponse<>() {

            @Override
            public String value() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public Optional<String> apply(ByteBuffer byteBuffer) {
                if (counter.incrementAndGet() == 3) {
                    throw new RuntimeException(exceptionMessage);
                }
                return Optional.empty();
            }
        };
    }
}
