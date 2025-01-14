package io.huskit.containers.http;

import io.huskit.common.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@RequiredArgsConstructor
final class DfHtHttpDockerSpec implements HtHttpDockerSpec {

    DockerSockets sockets;
    @With
    @Getter
    Boolean isCleanOnClose;
    @Getter
    HttpRequests requests;
    ScheduledExecutorService executor;
    @With
    Log log;
    @With
    @Getter
    Duration defaultTimeout;

    public DfHtHttpDockerSpec() {
        this(
            new DockerSockets(),
            false,
            new HttpRequests(),
            Executors.newScheduledThreadPool(2),
            Log.noop(),
            Duration.ZERO
        );
    }

    @Override
    public DockerSocket socket() {
        return sockets.pickDefault(executor, log);
    }

    @Override
    public Log log() {
        return log;
    }
}
