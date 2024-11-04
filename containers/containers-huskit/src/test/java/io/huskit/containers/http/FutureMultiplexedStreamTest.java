package io.huskit.containers.http;

import io.huskit.gradle.commontest.UnitTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class FutureMultiplexedStreamTest implements UnitTest {

    @Test
    void asd() throws Exception {
        var subject = new FutureMultiplexedStream(StreamType.ALL);
        var bytes = bodyBytesFromResource("/docker_http_responses/logs/all_stdout.txt");

        var maybeActual = subject.apply(ByteBuffer.wrap(bytes));

        assertThat(maybeActual).isPresent();
        var actual = maybeActual.get();
        var expectedLinesCount = 76;
        assertThat(actual.frames()).hasSize(expectedLinesCount);
        for (var idx = 0; idx < actual.frames().size(); idx++) {
            var frame = actual.frames().get(idx);
            assertThat(frame.type()).isEqualTo(FrameType.STDOUT);
            var frameText = new String(frame.data(), StandardCharsets.UTF_8);
            assertThat(frameText).isEqualTo("Hello world %s\n", idx);
        }
    }

    private byte[] bodyBytesFromResource(String resource) throws IOException {
        var bytes = IOUtils.resourceToByteArray(resource);
        char prev = 0;
        char prevPrev = 0;
        int indexOfBody = 0;
        for (int i = 0; i < bytes.length; i++) {
            var c = (char) bytes[i];
            if (c == '\n' && prev == '\r' && prevPrev == '\n') {
                indexOfBody = i + 1;
                break;
            }
            prevPrev = prev;
            prev = c;
        }
        return Arrays.copyOfRange(bytes, indexOfBody, bytes.length);
    }
}
