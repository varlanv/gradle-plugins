package io.huskit.containers.http;

import io.huskit.common.io.BufferLines;
import io.huskit.gradle.commontest.UnitTest;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
class DockerHttpMultiplexedStreamTest implements UnitTest {

    @Test
    void when_follow_logs__all_stdout__request_stdout__then_should_repeat_read_all() throws Exception {
        testWithFollow(
                "/docker_http_responses/logs/follow_all_stdout.txt",
                StreamType.STDOUT,
                FrameType.STDOUT,
                27,
                2
        );
    }

    @Test
    void when_follow_logs__all_stdout__request_stderr__then_should_read_empty() throws Exception {
        testWithFollowEmpty(
                "/docker_http_responses/logs/follow_all_stdout.txt",
                StreamType.STDERR
        );
    }

    @Test
    void when_not_follow_logs__all_stdout__request_stdout__should_read_all() throws Exception {
        testNonFollow(
                "/docker_http_responses/logs/all_stdout.txt",
                StreamType.STDOUT,
                FrameType.STDOUT,
                75
        );
    }

    @Test
    void when_not_follow_logs__all_stdout__request_stderr__then_should_read_empty() throws Exception {
        testNonFollowEmpty(
                "/docker_http_responses/logs/all_stdout.txt",
                StreamType.STDERR
        );
    }

    @Test
    void when_not_follow_logs__all_stderr__request_stderr__should_read_all() throws Exception {
        testNonFollow(
                "/docker_http_responses/logs/all_stderr.txt",
                StreamType.STDERR,
                FrameType.STDERR,
                29
        );
    }

    @Test
    void when_not_follow_logs__all_stderr__request_stdout__then_should_read_empty() throws Exception {
        testNonFollowEmpty(
                "/docker_http_responses/logs/all_stderr.txt",
                StreamType.STDOUT
        );
    }

    @Test
    void when_follow_logs__all_stderr__request_stderr__then_should_repeat_read_all() throws Exception {
        testWithFollow(
                "/docker_http_responses/logs/follow_all_stderr.txt",
                StreamType.STDERR,
                FrameType.STDERR,
                24,
                2
        );
    }

    @Test
    void when_follow_logs__all_stderr__request_stdout__then_should_read_empty() throws Exception {
        testWithFollowEmpty(
                "/docker_http_responses/logs/follow_all_stderr.txt",
                StreamType.STDOUT
        );
    }

    @Test
    void when_follow_logs__mix__request_stdout__then_should_return_only_stdout() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/follow_mix.txt",
                StreamType.STDOUT,
                readsCounter
        );
        var expectedElementsCount = 20;
        try (var actual = subject.get()) {
            ThrowingRunnable runnable = () -> {
                for (int idx = 0; idx <= expectedElementsCount; idx++) {
                    var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                    assertThat(frame)
                            .as("Frame %s", idx)
                            .isNotNull();
                    var frameBody = new String(frame.data(), StandardCharsets.UTF_8);
                    assertThat(frameBody)
                            .as("Frame %s", idx)
                            .isEqualTo("Hello world %s\n", idx == 0 ? 0 : idx * 2);
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
    void when_follow_logs__mix__request_stderr__then_should_return_only_stderr() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/follow_mix.txt",
                StreamType.STDERR,
                readsCounter
        );
        var expectedElementsCount = 21;
        try (var actual = subject.get()) {
            ThrowingRunnable runnable = () -> {
                for (int idx = 1; idx <= expectedElementsCount; idx++) {
                    var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                    assertThat(frame)
                            .as("Frame %s", idx)
                            .isNotNull();
                    var frameBody = new String(frame.data(), StandardCharsets.UTF_8);
                    assertThat(frameBody)
                            .as("Frame %s", idx)
                            .isEqualTo("Hello world %s\n", idx == 1 ? 1 : idx * 2 - 1);
                    assertThat(frame.type())
                            .isEqualTo(FrameType.STDERR);
                }
            };
            runnable.run();
            runnable.run();
            assertThat(readsCounter.get())
                    .isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void when_follow_logs__mix__request_all__then_should_return_all() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/follow_mix.txt",
                StreamType.ALL,
                readsCounter
        );
        var expectedElementsCount = 41;
        try (var actual = subject.get()) {
            ThrowingRunnable runnable = () -> {
                for (int idx = 0; idx <= expectedElementsCount; idx++) {
                    var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                    assertThat(frame)
                            .as("Frame %s", idx)
                            .isNotNull();
                    var frameBody = new String(frame.data(), StandardCharsets.UTF_8);
                    assertThat(frameBody)
                            .as("Frame %s", idx)
                            .isEqualTo("Hello world %s\n", idx);
                    assertThat(frame.type())
                            .isEqualTo(idx % 2 == 0 ? FrameType.STDOUT : FrameType.STDERR);
                }
            };
            runnable.run();
            runnable.run();
            assertThat(readsCounter.get())
                    .isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void when_non_follow_logs__mix__request_stdout__then_should_return_only_stdout() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/mix.txt",
                StreamType.STDOUT,
                readsCounter
        );
        var expectedElementsCount = 11;
        try (var actual = subject.get()) {
            for (int idx = 0; idx <= expectedElementsCount; idx++) {
                var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                assertThat(frame)
                        .as("Frame %s", idx)
                        .isNotNull();
                assertThat(new String(frame.data(), StandardCharsets.UTF_8))
                        .as("Frame %s", idx)
                        .isEqualTo("Hello world %s\n", idx == 0 ? 0 : idx * 2);
                assertThat(frame.type())
                        .isEqualTo(FrameType.STDOUT);
            }
            assertThat(actual.nextFrame(Duration.ofMillis(100)))
                    .isEmpty();
            assertThat(readsCounter.get())
                    .isEqualTo(1);
        }
    }

    @Test
    void when_non_follow_logs__mix__request_stderr__then_should_return_only_stderr() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/mix.txt",
                StreamType.STDERR,
                readsCounter
        );
        var expectedElementsCount = 11;
        try (var actual = subject.get()) {
            for (int idx = 1; idx <= expectedElementsCount; idx++) {
                var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                assertThat(frame)
                        .as("Frame %s", idx)
                        .isNotNull();
                assertThat(new String(frame.data(), StandardCharsets.UTF_8))
                        .as("Frame %s", idx)
                        .isEqualTo("Hello world %s\n", idx == 1 ? 1 : idx * 2 - 1);
                assertThat(frame.type())
                        .isEqualTo(FrameType.STDERR);
            }
            assertThat(actual.nextFrame(Duration.ofMillis(100)))
                    .isEmpty();
            assertThat(readsCounter.get())
                    .isEqualTo(1);
        }
    }

    @Test
    void when_non_follow_logs__mix__request_all__then_should_return_all() throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/mix.txt",
                StreamType.ALL,
                readsCounter
        );
        var expectedElementsCount = 22;
        try (var actual = subject.get()) {
            for (int idx = 0; idx <= expectedElementsCount; idx++) {
                var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                assertThat(frame)
                        .as("Frame %s", idx)
                        .isNotNull();
                assertThat(new String(frame.data(), StandardCharsets.UTF_8))
                        .as("Frame %s", idx)
                        .isEqualTo("Hello world %s\n", idx);
                assertThat(frame.type())
                        .isEqualTo(idx % 2 == 0 ? FrameType.STDOUT : FrameType.STDERR);
            }
            assertThat(actual.nextFrame(Duration.ofMillis(100)))
                    .isEmpty();
            assertThat(readsCounter.get())
                    .isEqualTo(1);
        }
    }

    @ParameterizedTest
    @EnumSource(StreamType.class)
    void when_non_follow_logs__empty__then_should_return_empty(StreamType streamType) throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/empty.txt",
                streamType,
                readsCounter
        );
        try (var actual = subject.get()) {
            assertThat(actual.nextFrame(Duration.ofMillis(100)))
                    .isEmpty();
            assertThat(readsCounter.get())
                    .isEqualTo(1);
        }
    }

    @ParameterizedTest
    @EnumSource(StreamType.class)
    void when_follow_logs__empty__then_should_return_empty(StreamType streamType) throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                "/docker_http_responses/logs/follow_empty.txt",
                streamType,
                readsCounter
        );
        try (var actual = subject.get()) {
            assertThat(actual.nextFrame(Duration.ofMillis(100)))
                    .isEmpty();
            assertThat(readsCounter.get())
                    .isGreaterThanOrEqualTo(2);
        }
    }

    @SneakyThrows
    private DockerHttpMultiplexedStream buildSubject(String resource,
                                                     StreamType streamType,
                                                     AtomicInteger readsCounter) {
        var bytes = IOUtils.resourceToByteArray(resource);
        var head = new HeadFromLines(
                new BufferLines(() -> bytes)
        );
        var idx = head.indexOfHeadEnd();
        return new DockerHttpMultiplexedStream(
                streamType,
                Executors.newSingleThreadExecutor(),
                () -> {
                    readsCounter.incrementAndGet();
                    return ByteBuffer.wrap(bytes, idx, bytes.length - idx);
                }
        );
    }

    private void testWithFollow(String resource,
                                StreamType streamType,
                                FrameType frameType,
                                Integer expectedElementsCount,
                                Integer expectedReads) throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                resource,
                streamType,
                readsCounter
        );
        try (var actual = subject.get()) {
            ThrowingRunnable runnable = () -> {
                for (int idx = 0; idx <= expectedElementsCount; idx++) {
                    var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                    assertThat(frame)
                            .as("Frame %s", idx)
                            .isNotNull();
                    assertThat(new String(frame.data(), StandardCharsets.UTF_8))
                            .as("Frame %s", idx)
                            .isEqualTo("Hello world %s\n", idx);
                    assertThat(frame.type())
                            .isEqualTo(frameType);
                }
            };
            runnable.run();
            runnable.run();
            assertThat(readsCounter.get())
                    .isGreaterThanOrEqualTo(expectedReads);
        }
    }

    private void testWithFollowEmpty(String resource, StreamType stderr) throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                resource,
                stderr,
                readsCounter
        );
        try (var actual = subject.get()) {
            assertThat(actual.nextFrame(Duration.ofMillis(100)))
                    .isEmpty();
            assertThat(readsCounter.get())
                    .isGreaterThanOrEqualTo(1);
        }
    }

    private void testNonFollow(String resource,
                               StreamType streamType,
                               FrameType frameType,
                               Integer expectedElementsCount) throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                resource,
                streamType,
                readsCounter
        );
        try (var actual = subject.get()) {
            for (int idx = 0; idx <= expectedElementsCount; idx++) {
                var frame = actual.nextFrame(Duration.ofSeconds(2)).orElseThrow();
                assertThat(frame)
                        .as("Frame %s", idx)
                        .isNotNull();
                assertThat(new String(frame.data(), StandardCharsets.UTF_8))
                        .as("Frame %s", idx)
                        .isEqualTo("Hello world %s\n", idx);
                assertThat(frame.type())
                        .isEqualTo(frameType);
            }
            assertThat(actual.nextFrame(Duration.ofMillis(100)))
                    .isEmpty();
            assertThat(readsCounter.get())
                    .isEqualTo(1);
        }
    }

    private void testNonFollowEmpty(String resource, StreamType stderr) throws Exception {
        var readsCounter = new AtomicInteger();
        var subject = buildSubject(
                resource,
                stderr,
                readsCounter
        );
        try (var actual = subject.get()) {
            assertThat(actual.nextFrame(Duration.ofMillis(100)))
                    .isEmpty();
            assertThat(readsCounter.get())
                    .isEqualTo(1);
        }
    }
}
