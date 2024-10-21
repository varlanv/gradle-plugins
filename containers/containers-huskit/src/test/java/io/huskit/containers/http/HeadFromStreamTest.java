package io.huskit.containers.http;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HeadFromStreamTest implements UnitTest {

    @Test
    void parse_head_success() {
        var httpHeadString = "HTTP/1.1 200 OK\r\n"
                + "Api-Version: 1.46\r\n"
                + "Content-Type: application/json\r\n"
                + "Date: Tue, 22 Oct 2024 01:15:00 GMT\r\n"
                + "Docker-Experimental: false\r\n"
                + "Ostype: linux\r\n"
                + "Server: Docker/27.0.3 (linux)\r\n"
                + "Content-Length: 3\r\n"
                + "\r\n"
                + "some body";

        var subject = new HeadFromStream(
                new ByteArrayInputStream(
                        httpHeadString.getBytes(StandardCharsets.UTF_8)
                )
        );

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
    }
}
