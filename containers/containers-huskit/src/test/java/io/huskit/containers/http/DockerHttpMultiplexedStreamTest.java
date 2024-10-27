package io.huskit.containers.http;

import io.huskit.common.io.BufferLines;
import io.huskit.gradle.commontest.UnitTest;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

class DockerHttpMultiplexedStreamTest implements UnitTest {

    @Test
    void a() throws Exception {
        var subject = buildSubject();

        var actual = subject.get();

        MultiplexedFrame peek = actual.frames().poll(2, TimeUnit.SECONDS);
        actual.stop();
        Thread.sleep(1000);
        System.out.println();
    }

    @SneakyThrows
    private DockerHttpMultiplexedStream buildSubject() {
        var bytes = IOUtils.resourceToByteArray("/logs.txt");
        var head = new HeadFromLines(
                new BufferLines(() -> bytes)
        );
        var idx = head.indexOfHeadEnd();
        return new DockerHttpMultiplexedStream(
                true,
                true,
                () -> Arrays.copyOfRange(bytes, idx, bytes.length)
        );
    }
}
