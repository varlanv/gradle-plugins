package io.huskit.containers.http;

import io.huskit.common.io.BufferLines;
import io.huskit.common.io.Line;
import io.huskit.common.io.Lines;
import io.huskit.gradle.commontest.UnitTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeadFromLinesTest implements UnitTest {

    String osTypeHeader = "linux".repeat(10000);

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
        var subject = new HeadFromLines(
                Lines.fromIterable(
                        headersLines().collect(Collectors.toList())
                )
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
        var httpHeadWithBodyString = Stream.concat(headersLines(), Stream.of("some body"))
                .collect(Collectors.toList());

        var subject = new HeadFromLines(
                Lines.fromIterable(
                        httpHeadWithBodyString
                )
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

    private String headers() {
        return "HTTP/1.1 200 OK\r\n"
                + "Api-Version: 1.46\r\n"
                + "Content-Type: application/json\r\n"
                + "Date: Tue, 22 Oct 2024 01:15:00 GMT\r\n"
                + "Docker-Experimental: false\r\n"
                + "Ostype: " + osTypeHeader + "\r\n"
                + "Server: Docker/27.0.3 (linux)\r\n"
                + "Content-Length: 3\r\n"
                + "\r\n";
    }

    private Stream<String> headersLines() {
        return headers().lines();
    }

    private void verifyHead(Http.Head subject) {
        assertThat(subject.status()).isEqualTo(200);
        assertThat(subject.isChunked()).isFalse();
        assertThat(subject.isMultiplexedStream()).isFalse();
        assertThat(subject.headers()).containsEntry("Api-Version", "1.46");
        assertThat(subject.headers()).containsEntry("Content-Type", "application/json");
        assertThat(subject.headers()).containsEntry("Date", "Tue, 22 Oct 2024 01:15:00 GMT");
        assertThat(subject.headers()).containsEntry("Docker-Experimental", "false");
        assertThat(subject.headers()).containsEntry("Ostype", osTypeHeader);
        assertThat(subject.headers()).containsEntry("Server", "Docker/27.0.3 (linux)");
        assertThat(subject.headers()).containsEntry("Content-Length", "3");
        assertThat(subject.headers()).hasSize(7);
    }
}
