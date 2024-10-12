package io.huskit;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Just some tests for educational purposes for JDK {@link java.nio.Buffer} classes.
 */
public class BufferTest implements UnitTest {

    @Test
    void byte_buffer() {
        var subject = ByteBuffer.allocate(512);

        assertThat(subject.position()).isEqualTo(0);
        assertThat(subject.limit()).isEqualTo(512);
        assertThat(subject.capacity()).isEqualTo(512);

        subject.putChar('a');
        assertThat(subject.position()).isEqualTo(2);
        subject.putInt(42);
        assertThat(subject.position()).isEqualTo(6);

        subject.flip();
        assertThat(subject.position()).isEqualTo(0);
        assertThat(subject.limit()).isEqualTo(6);

        assertThat(subject.getChar()).isEqualTo('a');
        assertThat(subject.getInt()).isEqualTo(42);
        assertThat(subject.position()).isEqualTo(6);

        assertThatThrownBy(subject::getInt)
                .isInstanceOf(BufferUnderflowException.class);

        subject.clear();
        assertThat(subject.position()).isEqualTo(0);
        assertThat(subject.limit()).isEqualTo(512);

        subject.putInt('\r');
        subject.putInt('\n');
        subject.flip();

        assertThat(subject.getInt()).isEqualTo(13);
        assertThat(subject.getInt()).isEqualTo(10);
    }

    @Test
    void char_buffer() {
        var subject = CharBuffer.allocate(512);

        subject.put('a');
        assertThat(subject.position()).isEqualTo(1);
        subject.put('b');
        assertThat(subject.position()).isEqualTo(2);

        subject.flip();

        assertThat(subject.get()).isEqualTo('a');
        assertThat(subject.position()).isEqualTo(1);
        assertThat(subject.get()).isEqualTo('b');
        assertThat(subject.position()).isEqualTo(2);

        assertThatThrownBy(subject::get)
                .isInstanceOf(BufferUnderflowException.class);
    }
}
