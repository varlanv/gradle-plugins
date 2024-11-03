package io.huskit.containers.http;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FutureHeadTest implements UnitTest {

    @Test
    void push__when_headers_are_present__should_return_head() {
        var subject = new FutureHead();
        var byteBuffer = ByteBuffer.wrap(headers().getBytes(StandardCharsets.UTF_8));

        var actual = subject.apply(byteBuffer);

        assertThat(actual).isPresent();
        verifyHead(actual.get(), byteBuffer, headers().length());
    }

    private String headers() {
        return "HTTP/1.1 200 OK\r\n"
                + "Api-Version: 1.46\r\n"
                + "Content-Type: application/json\r\n"
                + "Date: Tue, 22 Oct 2024 01:15:00 GMT\r\n"
                + "Docker-Experimental: false\r\n"
                + "Ostype: linux\r\n"
                + "Server: Docker/27.0.3 (linux)\r\n"
                + "Content-Length: 3\r\n"
                + "\r\n";
    }

    private void verifyHead(Http.Head head, ByteBuffer byteBuffer, Integer expectedPosition) {
        assertThat(head.status()).isEqualTo(200);
        assertThat(head.isChunked()).isFalse();
        assertThat(head.isMultiplexedStream()).isFalse();
        assertThat(head.headers()).containsEntry("Api-Version", "1.46");
        assertThat(head.headers()).containsEntry("Content-Type", "application/json");
        assertThat(head.headers()).containsEntry("Date", "Tue, 22 Oct 2024 01:15:00 GMT");
        assertThat(head.headers()).containsEntry("Docker-Experimental", "false");
        assertThat(head.headers()).containsEntry("Ostype", "linux");
        assertThat(head.headers()).containsEntry("Server", "Docker/27.0.3 (linux)");
        assertThat(head.headers()).containsEntry("Content-Length", "3");
        assertThat(head.headers()).hasSize(7);
        assertThat(head.isChunked()).isFalse();
        assertThat(head.isMultiplexedStream()).isFalse();
        assertThat(byteBuffer.position()).isEqualTo(expectedPosition);
    }
}
