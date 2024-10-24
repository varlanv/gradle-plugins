package io.huskit.containers.http;

import io.huskit.common.io.Line;
import io.huskit.common.io.Lines;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                        headersLines()
                )
        );

        verifyHead(subject);
    }
//
//    @Test
//    void parse_head_without_body__small_buffer__success() {
//        var httpHeadString = headers();
//
//        var subject = new HeadFromLines(
//                new LoopInputStream(
//                        () -> new ByteArrayInputStream(
//                                httpHeadString.getBytes(StandardCharsets.UTF_8)
//                        )
//                )
//        );
//
//        verifyHead(subject);
//    }
//
//    @Test
//    void parse_head_with_body__success() {
//        var httpHeadWithBodyString = headers()
//                + "some body";
//
//        var subject = new HeadFromLines(
//                new ByteBufferInputStream(
//                        ByteBuffer.wrap(httpHeadWithBodyString.getBytes(StandardCharsets.UTF_8))
//                )
//        );
//        subject.status();
//
//        verifyHead(subject);
//    }

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

    private List<String> headersLines() {
        return headers().lines().collect(Collectors.toList());
    }

    private void verifyHead(HeadFromLines subject) {
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
