package io.huskit.containers.http;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PushChunkedTest implements UnitTest {

    @Test
    void push__lf__when_body_is_present__should_return_chunked() {
        var expected = "Hello World";
        var body = "b\n" + expected + "\n0\n\n";
        var subject = new PushChunked<>(new PushRaw());

        var maybeActual = subject.push(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));

        assertThat(maybeActual).hasValue(expected);
    }

    @Test
    void push__crlf__when_body_is_present__should_return_chunked() {
        var expected = "Hello World";
        var body = "b\r\n" + expected + "\r\n0\r\n\r\n";
        var subject = new PushChunked<>(new PushRaw());

        var maybeActual = subject.push(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));

        assertThat(maybeActual).hasValue(expected);
    }
}
