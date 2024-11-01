package io.huskit.containers.http;

import io.huskit.common.io.BufferLines;
import io.huskit.gradle.commontest.UnitTest;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DockerHttpMultiplexedStreamTest implements UnitTest {

    @Test
    void when_follow_logs__all_stdout__request_stdout__then_should_repeat_read_all() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/follow_all_stdout.txt",
                StreamType.STDOUT,
                true,
                readsCounter
        );
        try (var actual = subject.get()) {
            var expectedElementsCountInFile = 27;
            ThrowingRunnable runnable = () -> {
                for (int idx = 0; idx <= expectedElementsCountInFile; idx++) {
                    var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                    assertThat(frame)
                            .as("Frame %s", idx)
                            .isNotNull();
                    assertThat(new String(frame.data(), StandardCharsets.UTF_8))
                            .as("Frame %s", idx)
                            .isEqualTo("Hello world %s\n", idx);
                    assertThat(frame.type())
                            .isEqualTo(FrameType.STDOUT);
                }
            };
            runnable.run();
            runnable.run();
            assertThat(readsCounter.get())
                    .isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void when_follow_logs__all_stdout__request_stderr__then_should_read_empty() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/follow_all_stdout.txt",
                StreamType.STDERR,
                true,
                readsCounter
        );
        try (var actual = subject.get()) {
            assertThat(actual.nextFrame(Duration.ofMillis(100))
                    .isEmpty());
            assertThat(readsCounter.get())
                    .isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void when_not_follow_logs__all_stdout__request_stdout__should_read_all() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/all_stdout.txt",
                StreamType.STDOUT,
                false,
                readsCounter
        );
        try (var actual = subject.get()) {
            var expectedElementsCountInFile = 75;
            for (int idx = 0; idx <= expectedElementsCountInFile; idx++) {
                var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                assertThat(frame)
                        .as("Frame %s", idx)
                        .isNotNull();
                assertThat(new String(frame.data(), StandardCharsets.UTF_8))
                        .as("Frame %s", idx)
                        .isEqualTo("Hello world %s\n", idx);
                assertThat(frame.type())
                        .isEqualTo(FrameType.STDOUT);
            }
            assertThat(actual.nextFrame(Duration.ofMillis(100))
                    .isEmpty());
            assertThat(readsCounter.get())
                    .isEqualTo(1);
        }
    }

    @Test
    void when_not_follow_logs__all_stdout__request_stderr__then_should_read_empty() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/all_stdout.txt",
                StreamType.STDERR,
                false,
                readsCounter
        );
        try (var actual = subject.get()) {
            assertThat(actual.nextFrame(Duration.ofMillis(100))
                    .isEmpty());
            assertThat(readsCounter.get())
                    .isEqualTo(1);
        }
    }


    @SneakyThrows
    private DockerHttpMultiplexedStream buildSubject(String resource, StreamType streamType, Boolean follow, AtomicInteger readsCounter) {
        var bytes = IOUtils.resourceToByteArray(resource);
        var head = new HeadFromLines(
                new BufferLines(() -> bytes)
        );
        var idx = head.indexOfHeadEnd();
        return new DockerHttpMultiplexedStream(
                streamType,
                () -> {
                    readsCounter.incrementAndGet();
                    return ByteBuffer.wrap(bytes, idx, bytes.length - idx);
                },
                follow
        );
    }
}
