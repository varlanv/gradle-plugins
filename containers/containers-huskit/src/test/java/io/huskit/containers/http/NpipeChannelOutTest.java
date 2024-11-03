package io.huskit.containers.http;

import io.huskit.common.function.ThrowingSupplier;
import io.huskit.gradle.commontest.UnitTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NpipeChannelOutTest implements UnitTest {

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void when_buffer_invalid__should_throw_exception(int bufferLength) {
        assertThatThrownBy(() -> new NpipeChannelOut(null, bufferLength))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Buffer size must be greater than 0");
    }

    @Test
    void readToBufferAsync__should_read_to_buffer_fully() {
        useChannel(channel -> {
            var data = "Hello";
            var subject = new NpipeChannelOut(channel, data.length());
            channel.write(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)), 0).get(5, TimeUnit.SECONDS);

            ThrowingSupplier<String> readOnce = () -> new String(
                    subject.readToBufferAsync().get(5, TimeUnit.SECONDS).array(),
                    StandardCharsets.UTF_8
            );
            assertThat(readOnce.get()).isEqualTo("Hello");
            assertThat(readOnce.get()).isEqualTo("Hello");
        });
    }

    @Test
    void readToBufferAsync__should_read_to_buffer_partially_when_not_enough_buffer_length() {
        useChannel(channel -> {
            var data = "Hello";
            var subject = new NpipeChannelOut(channel, 2);
            channel.write(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)), 0).get(5, TimeUnit.SECONDS);

            ThrowingSupplier<String> readOnce = () -> new String(
                    subject.readToBufferAsync().get(5, TimeUnit.SECONDS).array(),
                    StandardCharsets.UTF_8
            );
            assertThat(readOnce.get()).isEqualTo("He");
            assertThat(readOnce.get()).isEqualTo("He");
        });
    }

    @SneakyThrows
    private void useChannel(ThrowingConsumer<AsynchronousFileChannel> action) {
        useTempFile(file -> {
            try (var channel = AsynchronousFileChannel.open(
                    file,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.READ
            )) {
                action.accept(channel);
            }
        });
    }
}
