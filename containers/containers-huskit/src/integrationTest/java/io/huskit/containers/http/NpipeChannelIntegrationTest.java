package io.huskit.containers.http;

import io.huskit.common.FakeTestLog;
import io.huskit.gradle.commontest.DockerIntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledOnOs(OS.WINDOWS)
public class NpipeChannelIntegrationTest implements DockerIntegrationTest {

    int bufferSize = 4096;
    HttpRequests httpRequests = new HttpRequests();

    @Test
    void when_request_containers__then_status_200() {
        var url = "/containers/json?all=true";
        var log = new FakeTestLog();
        useSubject(
                new NpipeChannel(log, Executors.newScheduledThreadPool(1), bufferSize),
                subject -> {
                    var stream = subject.writeAndRead(
                            new Request(
                                    httpRequests.get(
                                            HtUrl.of(url)
                                    )
                            )
                    ).join();
                    var head = new HeadFromLines(stream);
                    assertThat(head.status()).isEqualTo(200);
                }
        );
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
