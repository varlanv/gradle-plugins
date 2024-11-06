package io.huskit.containers.http;

import io.huskit.common.Log;
import io.huskit.gradle.commontest.UnitTest;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

class NpipeChannelTest implements UnitTest {

    @NonFinal
    ScheduledExecutorService executor;
    String data = "Hello".repeat(250);
    byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

    @BeforeAll
    void setupAll() {
        executor = Executors.newScheduledThreadPool(1);
    }

    @AfterAll
    void cleanupAll() {
        executor.shutdownNow();
    }

    @Test
    void writeAndReadAsync__when_read_at_once__should_work_correctly() {
        var request = new PushRequest<>(
            bytes,
            PushResponse.fake(
                byteBuffer -> Optional.of(
                    new String(
                        byteBuffer.array(),
                        StandardCharsets.UTF_8
                    )
                )
            )
        );

        // when
        var actual = writeToSubject(request);

        then(actual).isEqualTo(data);
    }

    @Test
    void writeAndReadAsync__when_exception_is_thrown__should_propagate() {
        // given
        var expected = new RuntimeException("test");
        var request = new PushRequest<>(
            bytes,
            PushResponse.fake(
                byteBuffer -> {
                    throw expected;
                }
            )
        );

        thenThrownBy(() -> writeToSubject(request))
            .hasCause(expected);
    }

    @Test
    void writeAndReadAsync__when_read_in_parts__should_work_correctly() {
        // given
        var resultParts = new ConcurrentLinkedQueue<String>();
        var data = "data";
        var counter = new AtomicInteger(3);
        var request = new PushRequest<>(
            data.getBytes(StandardCharsets.UTF_8),
            PushResponse.fake(
                byteBuffer -> {
                    resultParts.add(new String(byteBuffer.array(), StandardCharsets.UTF_8));
                    if (counter.decrementAndGet() == 0) {
                        return Optional.of(resultParts);
                    }
                    return Optional.empty();
                }
            )
        );

        // when
        var actual = writeToSubject(request);

        // then
        then(actual).containsExactly(data, data, data);
    }

    @SneakyThrows
    private <T> T writeToSubject(PushRequest<T> pushRequest) {
        return useTempFile(
            file -> {
                try (var subject = new NpipeChannel(
                    file.toAbsolutePath().toString(),
                    executor,
                    Log.fakeVerbose(),
                    pushRequest.request().http().body().length)) {
                    return subject.writeAndReadAsync(pushRequest).get(2, TimeUnit.SECONDS);
                }
            }
        );
    }
}
