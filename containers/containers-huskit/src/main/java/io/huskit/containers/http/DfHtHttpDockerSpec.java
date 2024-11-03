package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

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

    public DfHtHttpDockerSpec() {
        this(new DockerSockets(), false, new HttpRequests(), Executors.newScheduledThreadPool(1));
    }

    @Override
    public DockerSocket socket() {
        return sockets.pickDefault(executor);
    }
}
