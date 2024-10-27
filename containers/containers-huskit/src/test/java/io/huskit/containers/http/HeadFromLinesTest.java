package io.huskit.containers.http;

import io.huskit.common.io.BufferLines;
import io.huskit.common.io.Line;
import io.huskit.gradle.commontest.UnitTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeadFromLinesTest implements UnitTest {

    @Test
    void parse_head__when_status_not_present_on_first_line__fails() {
        assertThatThrownBy(() ->
                new HeadFromLines(
                        () -> new Line("1")
                ).status()
        ).hasMessageContaining("Invalid status line: 1");
    }

    @Test
    void parse_head_without_body__success() {
        var bytes = headers().getBytes(StandardCharsets.UTF_8);

        var subject = new HeadFromLines(
                new BufferLines(() -> bytes)
        );

        verifyHead(subject);
    }

    @Test
    void parse_head_without_body__should_correctly_calculate_last_index() {
        var bytes = headers().getBytes(StandardCharsets.UTF_8);
        var subject = new HeadFromLines(
                new BufferLines(() -> bytes)
        );

        assertThat(subject.indexOfHeadEnd())
                .isNotNull()
                .isEqualTo(bytes.length);
    }

    @Test
    void parse_head_with_body__should_correctly_calculate_last_index() {
        var headers = headers();
        var bytes = (headers + "some body").getBytes(StandardCharsets.UTF_8);
        var subject = new HeadFromLines(
                new BufferLines(() -> bytes)
        );

        assertThat(subject.indexOfHeadEnd())
                .isNotNull()
                .isEqualTo(headers.length());
    }

    @Test
    void parse_head_with_body__success() {
        var headerBase = headers();
        var headersStr = headerBase + "\r\nsome body";
        var bytes = headersStr.getBytes(StandardCharsets.UTF_8);

        var subject = new HeadFromLines(
                new BufferLines(() -> bytes)
        );

        verifyHead(subject);
    }

    @Test
    void should_correctly_parse_head_from_logs_response() throws Exception {
        var bytes = IOUtils.resourceToByteArray("/logs.txt");

        var subject = new HeadFromLines(
                new BufferLines(() -> bytes)
        );

        assertThat(subject.status()).isEqualTo(200);
        assertThat(subject.isChunked()).isTrue();
        assertThat(subject.isMultiplexedStream()).isTrue();
        assertThat(subject.headers()).containsEntry("Api-Version", "1.46");
        assertThat(subject.headers()).containsEntry("Content-Type", "application/vnd.docker.multiplexed-stream");
        assertThat(subject.headers()).containsEntry("Date", "Fri, 25 Oct 2024 00:52:40 GMT");
        assertThat(subject.headers()).containsEntry("Docker-Experimental", "false");
        assertThat(subject.headers()).containsEntry("Ostype", "linux");
        assertThat(subject.headers()).containsEntry("Server", "Docker/27.0.3 (linux)");
        assertThat(subject.headers()).containsEntry("Transfer-Encoding", "chunked");
        assertThat(subject.headers()).hasSize(7);
        assertThat(subject.indexOfHeadEnd()).isEqualTo(234);
        assertThat(subject.isChunked()).isTrue();
        assertThat(subject.isMultiplexedStream()).isTrue();
    }

    @Test
    void when_header_last_crlf_is_split__should_correctly_parse() {
        var firstPart = ("HTTP/1.1 200 OK\r\n"
                + "Api-Version: 1.46\r\n"
                + "Content-Type: application/json\r\n"
                + "Date: Tue, 22 Oct 2024 01:15:00 GMT\r\n"
                + "Docker-Experimental: false\r\n"
                + "Ostype: linux\r\n"
                + "Server: Docker/27.0.3 (linux)\r\n"
                + "Content-Length: 3\r\n"
                + "\r").getBytes(StandardCharsets.UTF_8);
        var secondPart = "\n".getBytes(StandardCharsets.UTF_8);
        var counter = new AtomicInteger();

        var subject = new HeadFromLines(
                new BufferLines(() -> {
                    if (counter.incrementAndGet() == 1) {
                        return firstPart;
                    } else if (counter.get() == 2) {
                        return secondPart;
                    }
                    throw new IllegalStateException("Should not be called more than twice");
                })
        );

        verifyHead(subject);
    }

    @Test
    void when_header_split_in_three_parts__should_correctly_parse() {
        var firstPart = ("HTTP/1.1 200 OK\r\n"
                + "Api-Version: 1.46\r\n"
                + "Content-Type: application/json\r\n"
                + "Date: Tue, 22 Oct 2024 01:15:00 GMT\r\n"
                + "Docker-Experimental: false\r\n"
                + "Ostype: linux\r").getBytes(StandardCharsets.UTF_8);
        var secondPart = ("\n"
                + "Server: Docker/27.0.3 (linux)\r\n"
                + "Content-Length: 3\r\n"
                + "\r").getBytes(StandardCharsets.UTF_8);
        var thirdPart = "\n".getBytes(StandardCharsets.UTF_8);
        var counter = new AtomicInteger();

        var subject = new HeadFromLines(
                new BufferLines(() -> {
                    if (counter.incrementAndGet() == 1) {
                        return firstPart;
                    } else if (counter.get() == 2) {
                        return secondPart;
                    } else if (counter.get() == 3) {
                        return thirdPart;
                    }
                    throw new IllegalStateException("Should not be called more than three times");
                })
        );

        verifyHead(subject);
    }

    @Test
    void when_header_split_char_by_char__should_correctly_parse() {
        var bytes = headers().getBytes(StandardCharsets.UTF_8);

        var counter = new AtomicInteger();
        var subject = new HeadFromLines(
                new BufferLines(
                        () -> new byte[]{bytes[counter.getAndIncrement()]},
                        bytes.length + 1
                )
        );

        verifyHead(subject);
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

    private void verifyHead(HeadFromLines subject) {
        assertThat(subject.status()).isEqualTo(200);
        assertThat(subject.isChunked()).isFalse();
        assertThat(subject.isMultiplexedStream()).isFalse();
        assertThat(subject.headers()).containsEntry("Api-Version", "1.46");
        assertThat(subject.headers()).containsEntry("Content-Type", "application/json");
        assertThat(subject.headers()).containsEntry("Date", "Tue, 22 Oct 2024 01:15:00 GMT");
        assertThat(subject.headers()).containsEntry("Docker-Experimental", "false");
        assertThat(subject.headers()).containsEntry("Ostype", "linux");
        assertThat(subject.headers()).containsEntry("Server", "Docker/27.0.3 (linux)");
        assertThat(subject.headers()).containsEntry("Content-Length", "3");
        assertThat(subject.headers()).hasSize(7);
        assertThat(subject.indexOfHeadEnd()).isEqualTo(headers().length());
        assertThat(subject.isChunked()).isFalse();
        assertThat(subject.isMultiplexedStream()).isFalse();
    }
}
