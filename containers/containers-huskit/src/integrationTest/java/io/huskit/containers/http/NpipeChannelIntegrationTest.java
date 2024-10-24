package io.huskit.containers.http;

import io.huskit.common.FakeTestLog;
import io.huskit.gradle.commontest.DockerIntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

@EnabledOnOs(OS.WINDOWS)
public class NpipeChannelIntegrationTest implements DockerIntegrationTest {

    int bufferSize = 4096;
    HttpRequests httpRequests = new HttpRequests();

    @Test
    void asd() throws Exception {
        var url = "/containers/json?all=true";
        var log = new FakeTestLog();
        useSubject(new NpipeChannel(log, bufferSize), subject -> {
            var stream = subject.writeAndRead(
                    new Request(
                            httpRequests.get(
                                    HtUrl.of(url)
                            )
                    )
            ).join();
            var head = new HeadFromLines(stream);
            System.out.println(head.status());
        });
    }

    @SneakyThrows
    private void useSubject(NpipeChannel subject, ThrowingConsumer<NpipeChannel> action) {
        try {
            action.accept(subject);
        } finally {
            subject.channel().close();
        }
    }
}
