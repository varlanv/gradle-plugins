package io.huskit.containers.http;

import io.huskit.common.FakeTestLog;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class NpipeChannelTest implements UnitTest {

    @Test
    void writeAndReadAsync_should_work() {
        useTempFile(file -> {
            var executor = Executors.newScheduledThreadPool(1);
            var data = "Hello";
            try (var subject = new NpipeChannel(
                    file.toAbsolutePath().toString(),
                    executor,
                    new FakeTestLog(),
                    data.length())) {
                subject.writeAndReadAsync(data.getBytes(StandardCharsets.UTF_8), false)
                        .thenCompose(Supplier::get)
                        .thenAccept(bytes -> assertThat(new String(bytes.array(), StandardCharsets.UTF_8)).isEqualTo("Hello"))
                        .get(5, TimeUnit.SECONDS);
            } finally {
                executor.shutdownNow();
            }
        });
    }
}
