package io.huskit.common.io;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ByteBufferInputStreamTest implements UnitTest {

    @Test
    void when_null_buffer__throws() {
        assertThatThrownBy(() -> new ByteBufferInputStream(null)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void read__should_return_data() {
        var buffer = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        var subject = new ByteBufferInputStream(buffer);

        assertThat(subject.read()).isEqualTo('H');
        assertThat(subject.read()).isEqualTo('e');
        assertThat(subject.read()).isEqualTo('l');
        assertThat(subject.read()).isEqualTo('l');
        assertThat(subject.read()).isEqualTo('o');
        assertThat(subject.read()).isEqualTo(-1);
        assertThat(subject.read()).isEqualTo(-1);
    }

    @Test
    void read__when_empty_buffer__should_return_minus_one() {
        var buffer = ByteBuffer.wrap("".getBytes(StandardCharsets.UTF_8));
        var subject = new ByteBufferInputStream(buffer);

        assertThat(subject.read()).isEqualTo(-1);
    }

    @Test
    void read__to_byte_array__should_return_data() {
        var buffer = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        var subject = new ByteBufferInputStream(buffer);
        var bytes = new byte[5];

        assertThat(subject.read(bytes, 0, 5)).isEqualTo(5);
        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("Hello");
    }

    @Test
    void read__to_byte_array__when_not_enough_space__should_return_data() {
        var buffer = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        var subject = new ByteBufferInputStream(buffer);
        var bytes = new byte[3];

        assertThat(subject.read(bytes, 0, 3)).isEqualTo(3);
        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("Hel");
    }

    @Test
    void read__to_byte_array__when_not_enough_data__should_return_data() {
        var buffer = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        var subject = new ByteBufferInputStream(buffer);
        var bytes = new byte[10];

        assertThat(subject.read(bytes, 0, 10)).isEqualTo(5);
        assertThat(new String(bytes, 0, 5, StandardCharsets.UTF_8)).isEqualTo("Hello");
    }

    @Test
    void read__to_byte_array__when_empty_buffer__should_return_minus_one() {
        var buffer = ByteBuffer.wrap("".getBytes(StandardCharsets.UTF_8));
        var subject = new ByteBufferInputStream(buffer);
        var bytes = new byte[5];

        assertThat(subject.read(bytes, 0, 5)).isEqualTo(-1);
    }
}
